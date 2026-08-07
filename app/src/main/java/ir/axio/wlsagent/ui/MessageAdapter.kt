package ir.axio.wlsagent.ui

import android.graphics.Color
import android.view.Gravity
import ir.axio.wlsagent.network.MessageDto

class MessageAdapter : TextItemAdapter<MessageDto>(
    horizontalPadding = 28,
    verticalPadding = 18,
    textSizeSp = 15f
) {

    fun add(m: MessageDto) {
        items.add(m)
        notifyItemInserted(items.size - 1)
    }

    fun lastId(): Long = items.lastOrNull()?.id ?: 0L

    override fun bind(holder: VH, item: MessageDto) {
        holder.text.text = item.body
        // برای پشتیبان: پیام خودش (agent) سمت راست آبی، پیام کاربر سمت چپ خاکستری.
        val isMine = item.sender_type == "agent"
        holder.text.gravity = if (isMine) Gravity.END else Gravity.START
        holder.text.setBackgroundColor(if (isMine) Color.parseColor("#0B6CF0") else Color.parseColor("#EFEFEF"))
        holder.text.setTextColor(if (isMine) Color.WHITE else Color.BLACK)
    }
}
