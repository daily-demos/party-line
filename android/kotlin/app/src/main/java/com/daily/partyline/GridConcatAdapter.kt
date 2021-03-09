package com.daily.partyline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daily.partyline.databinding.ListItemInnerGridBinding

class GridConcatAdapter(var participantsAdapter: ParticipantsAdapter?) : RecyclerView.Adapter<GridConcatAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemInnerGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participantsAdapter)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_inner_grid
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(var binding: ListItemInnerGridBinding) : RecyclerView.ViewHolder(binding.getRoot()) {
        fun bind(adapter: ParticipantsAdapter?) {
            binding.childRecyclerView.adapter = adapter
        }
    }
}