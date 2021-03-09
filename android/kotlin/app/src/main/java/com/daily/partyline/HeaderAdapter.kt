package com.daily.partyline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daily.partyline.databinding.ListItemHeaderBinding

class HeaderAdapter(private val title: String?, private var timer: String?) : RecyclerView.Adapter<HeaderAdapter.ViewHolder?>() {

    fun updateTime(countDown: String?) {
        timer = countDown
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(title, timer)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_header
    }

    class ViewHolder(var binding: ListItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String?, timer: String?) {
            val headerTitle = binding.header
            headerTitle.text = title
            if (timer != null) {
                val timerView = binding.timer
                timerView.visibility = View.VISIBLE
                timerView.text = "Demo ends in $timer"
            } else {
                binding.separator.visibility = View.VISIBLE
            }
        }

    }
}