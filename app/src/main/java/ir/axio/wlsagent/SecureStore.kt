package ir.axio.wlsagent

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException

/**
 * ذخیره امن نام‌کاربری و Application Password وردپرس روی دستگاه.
 * توجه: این "رمز عبور اصلی" کاربر نیست؛ یک Application Password جداگانه است
 * که از پروفایل کاربر در wp-admin ساخته می‌شود و قابل ابطال جداگانه است.
 */
object SecureStore {
    private const val TAG = "SecureStore"
    private const val FILE_NAME = "wls_agent_secure_prefs"
    private const val KEY_USERNAME = "wp_username"
    private const val KEY_APP_PASSWORD = "wp_app_password"

    private fun createPrefs(ctx: Context): SharedPreferences = EncryptedSharedPreferences.create(
        ctx,
        FILE_NAME,
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * The encrypted store becomes unreadable when the master key is rotated or the keystore entry
     * is lost. Recreate it once in that case; a second failure is propagated to the caller.
     */
    private fun prefs(ctx: Context): SharedPreferences = try {
        createPrefs(ctx)
    } catch (e: GeneralSecurityException) {
        Log.e(TAG, "Encrypted preferences unreadable, recreating store", e)
        ctx.deleteSharedPreferences(FILE_NAME)
        createPrefs(ctx)
    }

    /** Returns false when the credentials could not be persisted. */
    fun saveCredentials(ctx: Context, username: String, appPassword: String): Boolean = try {
        prefs(ctx).edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_APP_PASSWORD, appPassword)
            .commit()
    } catch (e: Exception) {
        Log.e(TAG, "Storing credentials failed", e)
        false
    }

    private fun read(ctx: Context, key: String): String? = try {
        prefs(ctx).getString(key, null)
    } catch (e: Exception) {
        Log.e(TAG, "Reading $key failed", e)
        null
    }

    fun username(ctx: Context): String? = read(ctx, KEY_USERNAME)
    fun appPassword(ctx: Context): String? = read(ctx, KEY_APP_PASSWORD)

    fun isLoggedIn(ctx: Context): Boolean = username(ctx) != null && appPassword(ctx) != null

    fun clear(ctx: Context) {
        try {
            prefs(ctx).edit().clear().commit()
        } catch (e: Exception) {
            Log.e(TAG, "Clearing credentials failed, deleting store", e)
            ctx.deleteSharedPreferences(FILE_NAME)
        }
    }
}
