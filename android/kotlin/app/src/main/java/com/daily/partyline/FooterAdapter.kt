package com.daily.partyline

import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daily.partyline.databinding.ListItemFooterBinding

class FooterAdapter(var shareLink: String?) : RecyclerView.Adapter<FooterAdapter.ViewHolder?>() {

    fun setRoomUrl(shareLink: String?) {
        this.shareLink = shareLink
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shareLink)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_footer
    }

    class ViewHolder(var binding: ListItemFooterBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shareLink: String?) {
            binding.textLink.movementMethod = LinkMovementMethod.getInstance()
            binding.shareButton.setOnClickListener { sender: View? ->
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareLink)
                sendIntent.type = "text/plain"
                val shareIntent = Intent.createChooser(sendIntent, null)
                binding.getRoot().context.startActivity(shareIntent)
            }
        }

    }
}