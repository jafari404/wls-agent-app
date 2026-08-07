package ir.axio.wlsagent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ir.axio.wlsagent.network.RetrofitClient
import ir.axio.wlsagent.network.apiCall

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
                Toast.makeText(this, "نام‌کاربری و Application Password را وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SecureStore.saveCredentials(this, username, appPassword)
            RetrofitClient.reset()

            // اعتبارسنجی با یک درخواست آزمایشی؛ اگر 401/403 برگردد یعنی اطلاعات اشتباه است.
            apiCall({ it.listConversations() }) { result ->
                result.onSuccess {
                    goToConversations()
                }.onFailure {
                    SecureStore.clear(this)
                    RetrofitClient.reset()
                    Toast.makeText(this, "ورود ناموفق بود. اطلاعات را بررسی کنید.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToConversations() {
        startActivity(Intent(this, ConversationListActivity::class.java))
        finish()
    }
}
