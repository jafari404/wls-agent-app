package ir.axio.wlsagent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.axio.wlsagent.network.ApiException
import ir.axio.wlsagent.network.RetrofitClient
import ir.axio.wlsagent.network.apiCall
import ir.axio.wlsagent.network.toApiException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SecureStore.isLoggedIn(this)) {
            goToConversations()
            return
        }

        setContentView(R.layout.activity_login)

        val editUsername = findViewById<EditText>(R.id.editUsername)
        val editAppPassword = findViewById<EditText>(R.id.editAppPassword)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val username = editUsername.text.toString().trim()
            val appPassword = editAppPassword.text.toString().trim()
            if (username.isEmpty() || appPassword.isEmpty()) {
                Toast.makeText(this, R.string.error_credentials_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!SecureStore.saveCredentials(this, username, appPassword)) {
                Toast.makeText(this, R.string.error_credentials_not_saved, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            RetrofitClient.reset()

            // اعتبارسنجی با یک درخواست آزمایشی؛ اگر 401/403 برگردد یعنی اطلاعات اشتباه است.
            lifecycleScope.launch {
                apiCall("login") {
                    RetrofitClient.api(this@LoginActivity).listConversations()
                }.onSuccess {
                    goToConversations()
                }.onFailure { error ->
                    SecureStore.clear(this@LoginActivity)
                    RetrofitClient.reset()
                    // Wrong credentials and an unreachable server need different fixes, so the
                    // reason is shown instead of one generic message.
                    val apiError = error.toApiException()
                    val messageRes = if (apiError is ApiException.Unauthorized) {
                        R.string.error_login_failed
                    } else {
                        apiError.userMessageRes
                    }
                    Toast.makeText(this@LoginActivity, messageRes, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToConversations() {
        startActivity(Intent(this, ConversationListActivity::class.java))
        finish()
    }
}
