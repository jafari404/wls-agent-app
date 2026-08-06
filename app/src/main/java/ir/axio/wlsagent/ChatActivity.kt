package ir.axio.wlsagent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.axio.wlsagent.network.RetrofitClient
import ir.axio.wlsagent.network.SendMessageRequest
import ir.axio.wlsagent.ui.MessageAdapter
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private var conversationId: Long = -1L
    private val handler = Handler(Looper.getMainLooper())

    private val pollRunnable = object : Runnable {
        override fun run() {
            pollNewMessages()
            handler.postDelayed(this, 4000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        conversationId = intent.getLongExtra("conversation_id", -1L)
        title = intent.getStringExtra("guest_name") ?: "کاربر مهمان"

        val recycler = findViewById<RecyclerView>(R.id.recyclerMessages)
        adapter = MessageAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<android.widget.Button>(R.id.btnSend).setOnClickListener {
            val edit = findViewById<android.widget.EditText>(R.id.editMessage)
            val text = edit.text.toString().trim()
            if (text.isNotEmpty()) {
                edit.setText("")
                sendMessage(text)
            }
        }

        lifecycleScope.launch {
            runCatching { RetrofitClient.api(this@ChatActivity).claimConversation(conversationId) }
        }
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api(this@ChatActivity).getMessages(conversationId, afterId = 0)
            }.onSuccess { res ->
                adapter.setAll(res.messages)
                findViewById<RecyclerView>(R.id.recyclerMessages).scrollToPosition(maxOf(0, res.messages.size - 1))
            }
        }
    }

    private fun pollNewMessages() {
        if (conversationId <= 0) return
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api(this@ChatActivity).getMessages(conversationId, afterId = adapter.lastId())
            }.onSuccess { res ->
                res.messages.forEach { adapter.add(it) }
                if (res.messages.isNotEmpty()) {
                    findViewById<RecyclerView>(R.id.recyclerMessages).scrollToPosition(adapter.itemCount - 1)
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api(this@ChatActivity).postMessage(conversationId, SendMessageRequest(text))
                pollNewMessages()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(pollRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollRunnable)
    }
}
