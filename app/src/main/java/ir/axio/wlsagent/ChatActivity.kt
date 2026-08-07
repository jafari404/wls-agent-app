package ir.axio.wlsagent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.axio.wlsagent.network.SendMessageRequest
import ir.axio.wlsagent.network.apiCall
import ir.axio.wlsagent.network.apiCallOnSuccess
import ir.axio.wlsagent.ui.MessageAdapter
import ir.axio.wlsagent.util.PollingActivity

class ChatActivity : PollingActivity() {

    private lateinit var adapter: MessageAdapter
    private lateinit var recycler: RecyclerView
    private var conversationId: Long = -1L

    override val pollIntervalMs = 4000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        conversationId = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1L)
        title = intent.getStringExtra(EXTRA_GUEST_NAME) ?: getString(R.string.guest_default_name)

        recycler = findViewById(R.id.recyclerMessages)
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

        apiCall({ it.claimConversation(conversationId) })
        loadHistory()
    }

    override fun onPoll() = pollNewMessages()

    private fun loadHistory() {
        apiCallOnSuccess({ it.getMessages(conversationId, afterId = 0) }) { res ->
            adapter.setAll(res.messages)
            recycler.scrollToPosition(maxOf(0, res.messages.size - 1))
        }
    }

    private fun pollNewMessages() {
        if (conversationId <= 0) return
        apiCallOnSuccess({ it.getMessages(conversationId, afterId = adapter.lastId()) }) { res ->
            res.messages.forEach { adapter.add(it) }
            if (res.messages.isNotEmpty()) {
                recycler.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun sendMessage(text: String) {
        apiCall({ it.postMessage(conversationId, SendMessageRequest(text)) }) { pollNewMessages() }
    }

    companion object {
        private const val EXTRA_CONVERSATION_ID = "conversation_id"
        private const val EXTRA_GUEST_NAME = "guest_name"

        fun intent(context: Context, conversationId: Long, guestName: String?): Intent =
            Intent(context, ChatActivity::class.java)
                .putExtra(EXTRA_CONVERSATION_ID, conversationId)
                .putExtra(EXTRA_GUEST_NAME, guestName)
    }
}
