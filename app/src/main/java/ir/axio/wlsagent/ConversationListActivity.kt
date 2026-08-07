package ir.axio.wlsagent

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import ir.axio.wlsagent.network.RegisterDeviceRequest
import ir.axio.wlsagent.network.apiCall
import ir.axio.wlsagent.network.apiCallOnSuccess
import ir.axio.wlsagent.ui.ConversationAdapter
import ir.axio.wlsagent.util.PollingActivity

class ConversationListActivity : PollingActivity() {

    private lateinit var adapter: ConversationAdapter

    override val pollIntervalMs = 8000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        val recycler = findViewById<RecyclerView>(R.id.recyclerConversations)
        adapter = ConversationAdapter { conversation ->
            startActivity(ChatActivity.intent(this, conversation.id, conversation.guest_name))
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        registerPushToken()
    }

    override fun onPoll() = loadConversations()

    private fun registerPushToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            apiCall({ it.registerDevice(RegisterDeviceRequest(token)) })
        }
    }

    private fun loadConversations() {
        apiCallOnSuccess({ it.listConversations() }) { adapter.setAll(it) }
    }
}
