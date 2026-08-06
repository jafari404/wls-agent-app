package ir.axio.wlsagent.ui

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.axio.wlsagent.network.ConversationDto

class ConversationAdapter(
    private val items: MutableList<ConversationDto> = mutableListOf(),
    private val onClick: (ConversationDto) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    fun setAll(list: List<ConversationDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setPadding(32, 28, 32, 28)
            textSize = 16f
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.text.text = (c.guest_name ?: "کاربر مهمان") + "  •  " + (c.last_message_at ?: "")
        holder.text.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = items.size
}
