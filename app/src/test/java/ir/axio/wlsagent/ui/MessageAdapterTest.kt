package ir.axio.wlsagent.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.FrameLayout
import ir.axio.wlsagent.network.MessageDto
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class MessageAdapterTest {

    private fun message(id: Long, body: String = "hello", sender: String = "guest") =
        MessageDto(
            id = id,
            conversation_id = 7L,
            sender_type = sender,
            body = body,
            created_at = "2026-01-01 10:00:00"
        )

    private fun parent(): FrameLayout {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        return FrameLayout(activity)
    }

    /** The holder keeps the TextView's default vertical gravity, so only compare the horizontal part. */
    private fun horizontalGravity(holder: MessageAdapter.VH) =
        holder.text.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK

    private fun backgroundColor(holder: MessageAdapter.VH) =
        (holder.text.background as ColorDrawable).color

    private fun bind(adapter: MessageAdapter, position: Int): MessageAdapter.VH {
        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder, position)
        return holder
    }

    @Test
    fun `starts empty`() {
        val adapter = MessageAdapter()
        assertEquals(0, adapter.itemCount)
        assertEquals(0L, adapter.lastId())
    }

    @Test
    fun `setAll replaces previous items`() {
        val adapter = MessageAdapter()
        adapter.setAll(listOf(message(1), message(2)))
        adapter.setAll(listOf(message(9)))

        assertEquals(1, adapter.itemCount)
        assertEquals(9L, adapter.lastId())
    }

    @Test
    fun `add appends to the end`() {
        val adapter = MessageAdapter()
        adapter.setAll(listOf(message(1)))
        adapter.add(message(4))

        assertEquals(2, adapter.itemCount)
        assertEquals(4L, adapter.lastId())
    }

    @Test
    fun `lastId returns the id of the latest message`() {
        val adapter = MessageAdapter()
        adapter.setAll(listOf(message(3), message(11), message(8)))

        assertEquals(8L, adapter.lastId())
    }

    @Test
    fun `agent messages are aligned to the end with the agent colors`() {
        val adapter = MessageAdapter()
        adapter.setAll(listOf(message(1, body = "reply", sender = "agent")))

        val holder = bind(adapter, 0)

        assertEquals("reply", holder.text.text.toString())
        assertEquals(Gravity.END, horizontalGravity(holder))
        assertEquals(Color.WHITE, holder.text.currentTextColor)
        assertEquals(Color.parseColor("#0B6CF0"), backgroundColor(holder))
    }

    @Test
    fun `guest messages are aligned to the start with the guest colors`() {
        val adapter = MessageAdapter()
        adapter.setAll(listOf(message(1, body = "question", sender = "guest")))

        val holder = bind(adapter, 0)

        assertEquals("question", holder.text.text.toString())
        assertEquals(Gravity.START, horizontalGravity(holder))
        assertEquals(Color.BLACK, holder.text.currentTextColor)
        assertEquals(Color.parseColor("#EFEFEF"), backgroundColor(holder))
    }

    @Test
    fun `a recycled holder is fully re-styled for the new message`() {
        val adapter = MessageAdapter()
        adapter.setAll(listOf(message(1, sender = "agent"), message(2, sender = "guest")))

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder, 0)
        adapter.onBindViewHolder(holder, 1)
        ShadowLooper.idleMainLooper()

        assertEquals(Gravity.START, horizontalGravity(holder))
        assertEquals(Color.BLACK, holder.text.currentTextColor)
        assertEquals(Color.parseColor("#EFEFEF"), backgroundColor(holder))
    }
}
