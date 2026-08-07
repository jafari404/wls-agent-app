package ir.axio.wlsagent.ui

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * آداپتور پایه برای لیست‌هایی که هر ردیف آن‌ها فقط یک [TextView] است.
 */
abstract class TextItemAdapter<T>(
    protected val items: MutableList<T> = mutableListOf(),
    private val horizontalPadding: Int,
    private val verticalPadding: Int,
    private val textSizeSp: Float
) : RecyclerView.Adapter<TextItemAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    @Suppress("NotifyDataSetChanged")
    fun setAll(list: List<T>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            textSize = textSizeSp
        }
        return VH(tv)
    }

    final override fun onBindViewHolder(holder: VH, position: Int) = bind(holder, items[position])

    protected abstract fun bind(holder: VH, item: T)

    final override fun getItemCount() = items.size
}
