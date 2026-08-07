package ir.axio.wlsagent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.axio.wlsagent.network.RetrofitClient
import ir.axio.wlsagent.network.SendMessageRequest
import ir.axio.wlsagent.network.apiCall
import ir.axio.wlsagent.network.requireOk
import ir.axio.wlsagent.ui.MessageAdapter
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private var conversationId: Long = -1L
    /** Keeps the 4 second poll loop from flooding the user with the same error. */
    private var pollErrorReported = false
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
        if (conversationId <= 0) {
            Toast.makeText(this, R.string.error_invalid_conversation, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        title = intent.getStringExtra("guest_name") ?: "کاربر مهمان"

        val recycler = findViewById<RecyclerView>(R.id.recyclerMessages)
        adapter = MessageAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val edit = findViewById<EditText>(R.id.editMessage)
            val text = edit.text.toString().trim()
            if (text.isNotEmpty()) {
                edit.setText("")
                sendMessage(text)
            }
        }

        lifecycleScope.launch {
            apiCall("claimConversation") {
                RetrofitClient.api(this@ChatActivity)
                    .claimConversation(conversationId)
                    .requireOk("claimConversation")
            }.onFailure { showApiError(it) }
        }
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            apiCall("getMessages") {
                RetrofitClient.api(this@ChatActivity).getMessages(conversationId, afterId = 0)
            }.onSuccess { res ->
                adapter.setAll(res.messages)
                findViewById<RecyclerView>(R.id.recyclerMessages).scrollToPosition(maxOf(0, res.messages.size - 1))
            }.onFailure { showApiError(it) }
        }
    }

    private fun pollNewMessages() {
        if (conversationId <= 0) return
        lifecycleScope.launch {
            apiCall("getMessages") {
                RetrofitClient.api(this@ChatActivity).getMessages(conversationId, afterId = adapter.lastId())
            }.onSuccess { res ->
                pollErrorReported = false
                res.messages.forEach { adapter.add(it) }
                if (res.messages.isNotEmpty()) {
                    findViewById<RecyclerView>(R.id.recyclerMessages).scrollToPosition(adapter.itemCount - 1)
                }
            }.onFailure { error ->
                if (!pollErrorReported) {
                    pollErrorReported = true
                    showApiError(error)
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        val sendButton = findViewById<Button>(R.id.btnSend)
        sendButton.isEnabled = false
        lifecycleScope.launch {
            apiCall("postMessage") {
                RetrofitClient.api(this@ChatActivity)
                    .postMessage(conversationId, SendMessageRequest(text))
                    .requireOk("postMessage")
            }.onSuccess {
                pollNewMessages()
            }.onFailure { error ->
                // Put the text back so a failed send does not lose what the agent typed.
                val edit = findViewById<EditText>(R.id.editMessage)
                if (edit.text.isEmpty()) {
                    edit.setText(text)
                    edit.setSelection(text.length)
                }
                showApiError(error)
            }
            sendButton.isEnabled = true
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
