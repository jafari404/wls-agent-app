package ir.axio.wlsagent.ui

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.axio.wlsagent.network.MessageDto

class MessageAdapter(private val items: MutableList<MessageDto> = mutableListOf()) :
    RecyclerView.Adapter<MessageAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    fun setAll(list: List<MessageDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun add(m: MessageDto) {
        items.add(m)
        notifyItemInserted(items.size - 1)
    }

    fun lastId(): Long = items.lastOrNull()?.id ?: 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setPadding(28, 18, 28, 18)
            textSize = 15f
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]
        holder.text.text = m.body
        // برای پشتیبان: پیام خودش (agent) سمت راست آبی، پیام کاربر سمت چپ خاکستری.
        val isMine = m.sender_type == "agent"
        holder.text.gravity = if (isMine) Gravity.END else Gravity.START
        holder.text.setBackgroundColor(if (isMine) Color.parseColor("#0B6CF0") else Color.parseColor("#EFEFEF"))
        holder.text.setTextColor(if (isMine) Color.WHITE else Color.BLACK)
    }

    override fun getItemCount() = items.size
}
