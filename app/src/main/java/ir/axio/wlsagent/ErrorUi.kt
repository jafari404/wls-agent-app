package ir.axio.wlsagent

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.axio.wlsagent.network.ApiException
import ir.axio.wlsagent.network.RetrofitClient
import ir.axio.wlsagent.network.toApiException

/**
 * Reports [error] to the user. Authentication failures also drop the stored credentials and send
 * the user back to the login screen instead of leaving the app in a permanently failing state.
 */
fun AppCompatActivity.showApiError(error: Throwable) {
    val apiError = error.toApiException()
    Toast.makeText(this, getString(apiError.userMessageRes), Toast.LENGTH_LONG).show()
    if (apiError is ApiException.Unauthorized) signOut()
}

fun AppCompatActivity.signOut() {
    SecureStore.clear(this)
    RetrofitClient.reset()
    startActivity(
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    )
    finish()
}
