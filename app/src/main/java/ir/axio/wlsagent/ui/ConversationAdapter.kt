package ir.axio.wlsagent.ui

import ir.axio.wlsagent.R
import ir.axio.wlsagent.network.ConversationDto

class ConversationAdapter(
    private val onClick: (ConversationDto) -> Unit
) : TextItemAdapter<ConversationDto>(
    horizontalPadding = 32,
    verticalPadding = 28,
    textSizeSp = 16f
) {

    override fun bind(holder: VH, item: ConversationDto) {
        val name = item.guest_name ?: holder.text.context.getString(R.string.guest_default_name)
        holder.text.text = name + "  •  " + (item.last_message_at ?: "")
        holder.text.setOnClickListener { onClick(item) }
    }
}
