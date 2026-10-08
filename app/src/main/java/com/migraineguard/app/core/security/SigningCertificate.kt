package com.migraineguard.app.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/** Reads the certificate of the installed APK, never a remembered build-machine fingerprint. */
@Suppress("DEPRECATION")
fun installedSigningSha1(context: Context): String = try {
    val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_SIGNING_CERTIFICATES,
        ).signingInfo?.apkContentsSigners.orEmpty()
    } else {
        context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_SIGNATURES,
        ).signatures.orEmpty()
    }
    signatures.map { signature ->
        MessageDigest.getInstance("SHA-1").digest(signature.toByteArray())
            .joinToString(":") { "%02X".format(it.toInt() and 0xff) }
    }.distinct().sorted().joinToString("\n").ifBlank { "Unavailable" }
} catch (_: Exception) {
    "Unavailable"
}
