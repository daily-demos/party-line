package com.daily.partyline;

import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daily.partyline.databinding.ListItemFooterBinding;

public class FooterAdapter extends RecyclerView.Adapter<FooterAdapter.ViewHolder> {

    public String mShareLink;
    ListItemFooterBinding mBinding;

    public FooterAdapter(String shareLink) {
        mShareLink = shareLink;
    }

    public void setRoomUrl(String shareLink) {
        mShareLink = shareLink;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FooterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mBinding = ListItemFooterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FooterAdapter.ViewHolder holder, int position) {
        holder.bind(mShareLink);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.list_item_footer;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ListItemFooterBinding mBinding;

        public ViewHolder(@NonNull ListItemFooterBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(String shareLink) {
            mBinding.textLink.setMovementMethod(LinkMovementMethod.getInstance());
            mBinding.shareButton.setOnClickListener((sender) -> {
                String roomCode = shareLink;

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, roomCode);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                mBinding.getRoot().getContext().startActivity(shareIntent);
            });
        }
    }
}
