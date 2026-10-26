package com.migraineguard.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AppUpdates {
    const val REPO = "https://github.com/jdp323/migraineguard"
    const val LATEST = REPO+"/releases/latest"
    const val ENDPOINT = "https://api.github.com/repos/jdp323/migraineguard/releases/latest"
    data class Release(val version: String, val url: String, val notes: String)
    fun versionParts(value: String): List<Int>? = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:[-+][A-Za-z0-9.-]+)?$").matchEntire(value.trim())?.groupValues?.drop(1)?.map { it.toIntOrNull() ?: return null }
    fun newer(latest: String, current: String): Boolean {
        val a = versionParts(latest) ?: return false; val b = versionParts(current) ?: return false
        for(i in 0..2) { if(a[i]!=b[i]) return a[i]>b[i] }; return false
    }
    fun parse(body: String): Release {
        val obj = JSONObject(body)
        require(!obj.optBoolean("draft") && !obj.optBoolean("prerelease")) { "No stable release found." }
        val version = obj.getString("tag_name"); require(versionParts(version) != null)
        val url = obj.getString("html_url")
        require(url == REPO+"/releases/tag/"+version)
        val assets = obj.getJSONArray("assets")
        require((0 until assets.length()).any { assets.getJSONObject(it).optString("name").endsWith(".apk") }) { "The latest release has no APK yet." }
        return Release(version, url, obj.optString("body").take(6000))
    }
    suspend fun check(context: Context): Release = withContext(Dispatchers.IO) {
        val http = OkHttpClient.Builder().followRedirects(false).connectTimeout(12,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).callTimeout(25,TimeUnit.SECONDS).build()
        val req = Request.Builder().url(ENDPOINT).header("Accept","application/vnd.github+json").header("User-Agent","MigraineGuard/"+BuildConfig.VERSION_NAME).build()
        http.newCall(req).execute().use { response ->
            check(response.isSuccessful) { if(response.code==403||response.code==429) "GitHub rate limit reached. Try again later." else "Update check failed (HTTP "+response.code+")." }
            val bytes = response.body?.byteStream()?.use { it.readBytesBounded(512_000) } ?: error("Empty update response")
            val release = parse(String(bytes,Charsets.UTF_8))
            context.getSharedPreferences("public_release_cache",Context.MODE_PRIVATE).edit()
                .putString("version",release.version).putString("url",release.url).putString("notes",release.notes).putLong("checked",System.currentTimeMillis()).apply()
            release
        }
    }
    fun cached(context: Context): Release? {
        val p = context.getSharedPreferences("public_release_cache",Context.MODE_PRIVATE)
        val v = p.getString("version",null) ?: return null
        val u = p.getString("url",null) ?: return null
        return Release(v,u,p.getString("notes","").orEmpty()).takeIf { versionParts(v)!=null && u.startsWith(REPO+"/releases/tag/") }
    }
}
private fun java.io.InputStream.readBytesBounded(limit: Int): ByteArray {
    val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
    while(true){val n=read(buffer);if(n<0)break;require(out.size()+n<=limit);out.write(buffer,0,n)};return out.toByteArray()
}

@Composable
fun AppUpdateCard() {
    val context=LocalContext.current; val scope=rememberCoroutineScope()
    var release by remember { mutableStateOf(AppUpdates.cached(context)) }
    var busy by remember { mutableStateOf(false) };var message by remember { mutableStateOf("") };var details by remember { mutableStateOf(false) }
    SectionCard("App updates") {
        Text("Installed: "+BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")")
        release?.let { Text(if(AppUpdates.newer(it.version,BuildConfig.VERSION_NAME)) "New version available: "+it.version else "Latest checked release: "+it.version) }
        Text("Checks the public GitHub release. Nothing is installed automatically.", style=MaterialTheme.typography.bodySmall)
        Button(onClick={scope.launch{busy=true;message="Checking GitHub…";try{release=AppUpdates.check(context);message=if(AppUpdates.newer(release!!.version,BuildConfig.VERSION_NAME)) "An update is available." else "You have the latest published version or a newer local build."}catch(e:CancellationException){throw e}catch(e:Exception){message=e.message?:"Could not check. Your current app is unchanged."}finally{busy=false}}},enabled=!busy,modifier=Modifier.testTag("check-updates")){Text(if(busy)"Checking…" else "Check for updates")}
        if(message.isNotBlank())Text(message,Modifier.testTag("update-status"))
        val checked=context.getSharedPreferences("public_release_cache",Context.MODE_PRIVATE).getLong("checked",0)
        if(checked>0) Text("Last successful check: "+java.time.Instant.ofEpochMilli(checked).atZone(java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("MMM d, HH:mm")),style=MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick={runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(release?.url?:AppUpdates.LATEST)))}}){Text(if(release?.let{AppUpdates.newer(it.version,BuildConfig.VERSION_NAME)}==true)"Download update on GitHub" else "Open downloads on GitHub")}
        if(release!=null){TextButton(onClick={details=!details}){Text(if(details)"Hide release notes" else "What's changed?")};if(details)Text(release!!.notes,style=MaterialTheme.typography.bodySmall)}
    }
}
