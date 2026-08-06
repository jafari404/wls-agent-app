package ir.axio.wlsagent

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * ذخیره امن نام‌کاربری و Application Password وردپرس روی دستگاه.
 * توجه: این "رمز عبور اصلی" کاربر نیست؛ یک Application Password جداگانه است
 * که از پروفایل کاربر در wp-admin ساخته می‌شود و قابل ابطال جداگانه است.
 */
object SecureStore {
    private const val FILE_NAME = "wls_agent_secure_prefs"
    private const val KEY_USERNAME = "wp_username"
    private const val KEY_APP_PASSWORD = "wp_app_password"

    private fun prefs(ctx: Context) = EncryptedSharedPreferences.create(
        ctx,
        FILE_NAME,
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(ctx: Context, username: String, appPassword: String) {
        prefs(ctx).edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_APP_PASSWORD, appPassword)
            .apply()
    }

    fun username(ctx: Context): String? = prefs(ctx).getString(KEY_USERNAME, null)
    fun appPassword(ctx: Context): String? = prefs(ctx).getString(KEY_APP_PASSWORD, null)

    fun isLoggedIn(ctx: Context): Boolean = username(ctx) != null && appPassword(ctx) != null

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }
}
