package com.migraineguard.app.feature.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class DriveFile(val id: String, val etag: String?, val bytes: ByteArray)

interface DriveBackupTransport {
    suspend fun verifyAccount(token: String, email: String)
    suspend fun download(token: String): DriveFile?
    suspend fun upload(token: String, payload: ByteArray, previous: DriveFile?): String
}

class DriveRestClient(
    private val baseUrl: String = "https://www.googleapis.com",
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS).build(),
) : DriveBackupTransport {
    private fun request(url: String, token: String) = Request.Builder().url(url)
        .header("Authorization", "Bearer " + token)
    private fun checked(response: Response): Response {
        if (!response.isSuccessful) {
            val code = response.code
            response.close()
            error(when (code) {
                401 -> "Drive authorization expired. Reconnect Google Drive."
                403 -> "Drive access was denied. Allow app-data backup access and ensure the Google Drive API is enabled."
                412 -> "Your Drive backup changed on another device. Restore again before saving."
                else -> "Google Drive request failed (HTTP " + code + "). Your local data is unchanged."
            })
        }
        return response
    }
    private fun bytes(response: Response): ByteArray {
        val body = response.body ?: error("Empty Drive response")
        require(body.contentLength() <= CloudBackupFormat.MAX_BYTES)
        return body.byteStream().use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                require(output.size() + count <= CloudBackupFormat.MAX_BYTES) { "Drive backup is too large." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    }

    override suspend fun verifyAccount(token: String, email: String) = withContext(Dispatchers.IO) {
        http.newCall(request(baseUrl + "/oauth2/v3/userinfo", token).build()).execute().use { response ->
            val user = JSONObject(String(bytes(checked(response)), Charsets.UTF_8))
            require(user.optString("email").equals(email.trim(), ignoreCase = true) &&
                user.optBoolean("email_verified")) { "Drive account does not match the signed-in app account." }
        }
    }

    override suspend fun download(token: String): DriveFile? = withContext(Dispatchers.IO) {
        val url = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments("drive/v3/files").addQueryParameter("spaces", "appDataFolder")
            .addQueryParameter("q", "name = '" + CloudBackupFormat.FILE_NAME + "' and trashed = false")
            .addQueryParameter("fields", "files(id,modifiedTime)")
            .addQueryParameter("pageSize", "100").addQueryParameter("orderBy", "modifiedTime desc").build()
        val id = http.newCall(request(url.toString(), token).build()).execute().use { response ->
            val files = JSONObject(String(bytes(checked(response)), Charsets.UTF_8)).getJSONArray("files")
            if (files.length() == 0) null else files.getJSONObject(0).getString("id")
        } ?: return@withContext null
        require(id.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid Drive file identifier." }
        http.newCall(request(baseUrl + "/drive/v3/files/" + id + "?alt=media", token).build())
            .execute().use { response ->
                checked(response)
                DriveFile(id, response.header("ETag"), bytes(response))
            }
    }

    override suspend fun upload(token: String, payload: ByteArray, previous: DriveFile?): String =
        withContext(Dispatchers.IO) {
            require(payload.size <= CloudBackupFormat.MAX_BYTES)
            val jsonType = "application/json; charset=utf-8".toMediaType()
            val builder = if (previous == null) {
                val metadata = JSONObject().put("name", CloudBackupFormat.FILE_NAME)
                    .put("parents", org.json.JSONArray().put("appDataFolder")).toString()
                val body = MultipartBody.Builder().setType("multipart/related".toMediaType())
                    .addPart(metadata.toRequestBody(jsonType))
                    .addPart(payload.toRequestBody(jsonType)).build()
                request(baseUrl + "/upload/drive/v3/files?uploadType=multipart&fields=id", token).post(body)
            } else {
                request(baseUrl + "/upload/drive/v3/files/" + previous.id + "?uploadType=media&fields=id", token)
                    .patch(payload.toRequestBody(jsonType)).apply { previous.etag?.let { header("If-Match", it) } }
            }
            http.newCall(builder.build()).execute().use { response ->
                JSONObject(String(bytes(checked(response)), Charsets.UTF_8)).getString("id")
            }
        }
}
