package com.migraineguard.app.feature.auth

import android.content.Context
import com.migraineguard.app.core.domain.UserProfile
import com.migraineguard.app.core.security.ApiKeyVault
import org.json.JSONObject

/** Remember the chosen local session, not Google access/refresh/ID tokens. Not portable or backed up. */
class RememberedSession(context: Context, fileName: String = "remembered_session") {
    private val store = context.getSharedPreferences(fileName,Context.MODE_PRIVATE)
    private val vault = ApiKeyVault()
    fun save(state: AuthState) {
        if (!state.isSignedIn) { clear(); return }
        val user = requireNotNull(state.user)
        val value = JSONObject().put("local",state.isDemo).put("id",user.id)
            .put("name",user.displayName.take(200)).put("email",user.email.orEmpty()).toString()
        store.edit().putString("session",vault.encrypt(value)).commit()
    }
    fun load(): AuthState? = runCatching {
        val raw = store.getString("session",null) ?: return null
        require(raw.length <= 16_000 && raw.startsWith(ApiKeyVault.PREFIX))
        val value = JSONObject(vault.decrypt(raw))
        if (value.getBoolean("local")) AuthState(true,UserProfile("demo-user","Demo User","demo@migraineguard.app"),true)
        else {
            val id = value.getString("id"); val email = value.getString("email")
            require(id.isNotBlank() && id.length <= 320 && id.equals(email,true) && email.contains('@'))
            AuthState(true,UserProfile(id,value.getString("name").take(200),email),false)
        }
    }.getOrElse { clear(); null }
    fun clear() { store.edit().remove("session").commit() }
}
