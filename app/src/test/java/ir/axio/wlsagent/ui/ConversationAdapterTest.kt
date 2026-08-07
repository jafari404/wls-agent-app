package ir.axio.wlsagent.ui

import android.app.Activity
import android.widget.FrameLayout
import ir.axio.wlsagent.network.ConversationDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConversationAdapterTest {

    private fun conversation(
        id: Long = 1L,
        guestName: String? = "Ali",
        lastMessageAt: String? = "2026-01-01 10:00:00"
    ) = ConversationDto(
        id = id,
        guest_name = guestName,
        status = "open",
        last_message_at = lastMessageAt,
        assigned_agent_id = null
    )

    private fun parent(): FrameLayout {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        return FrameLayout(activity)
    }

    private fun bind(adapter: ConversationAdapter, position: Int): ConversationAdapter.VH {
        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder, position)
        return holder
    }

    @Test
    fun `starts empty`() {
        assertEquals(0, ConversationAdapter(onClick = {}).itemCount)
    }

    @Test
    fun `setAll replaces previous items`() {
        val adapter = ConversationAdapter(onClick = {})
        adapter.setAll(listOf(conversation(1), conversation(2)))
        adapter.setAll(listOf(conversation(3)))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `binds the guest name and the last message time`() {
        val adapter = ConversationAdapter(onClick = {})
        adapter.setAll(listOf(conversation(guestName = "Ali", lastMessageAt = "2026-01-01 10:00:00")))

        val text = bind(adapter, 0).text.text.toString()

        assertTrue(text.contains("Ali"))
        assertTrue(text.contains("2026-01-01 10:00:00"))
    }

    @Test
    fun `falls back to a placeholder name and an empty time`() {
        val adapter = ConversationAdapter(onClick = {})
        adapter.setAll(listOf(conversation(guestName = null, lastMessageAt = null)))

        assertEquals("کاربر مهمان  •  ", bind(adapter, 0).text.text.toString())
    }

    @Test
    fun `clicking a row reports the bound conversation`() {
        val clicked = mutableListOf<ConversationDto>()
        val adapter = ConversationAdapter(onClick = { clicked.add(it) })
        adapter.setAll(listOf(conversation(id = 1), conversation(id = 2)))

        bind(adapter, 1).text.performClick()

        assertEquals(listOf(2L), clicked.map { it.id })
    }

    @Test
    fun `a recycled holder reports the conversation it was last bound to`() {
        val clicked = mutableListOf<ConversationDto>()
        val adapter = ConversationAdapter(onClick = { clicked.add(it) })
        adapter.setAll(listOf(conversation(id = 1), conversation(id = 2)))

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder, 0)
        adapter.onBindViewHolder(holder, 1)
        holder.text.performClick()

        assertEquals(listOf(2L), clicked.map { it.id })
    }
}
