package ir.axio.wlsagent

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import ir.axio.wlsagent.network.RegisterDeviceRequest
import ir.axio.wlsagent.network.RetrofitClient
import ir.axio.wlsagent.ui.ConversationAdapter
import kotlinx.coroutines.launch

class ConversationListActivity : AppCompatActivity() {

    private lateinit var adapter: ConversationAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadConversations()
            handler.postDelayed(this, 8000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        val recycler = findViewById<RecyclerView>(R.id.recyclerConversations)
        adapter = ConversationAdapter { conversation ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("conversation_id", conversation.id)
            intent.putExtra("guest_name", conversation.guest_name)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        registerPushToken()
    }

    private fun registerPushToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            lifecycleScope.launch {
                runCatching { RetrofitClient.api(this@ConversationListActivity).registerDevice(RegisterDeviceRequest(token)) }
            }
        }
    }

    private fun loadConversations() {
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api(this@ConversationListActivity).listConversations()
            }.onSuccess { adapter.setAll(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }
}
