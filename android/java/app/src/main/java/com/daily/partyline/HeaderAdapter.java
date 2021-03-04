package com.daily.partyline;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daily.partyline.databinding.ListItemFooterBinding;
import com.daily.partyline.databinding.ListItemHeaderBinding;

public class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.ViewHolder> {

    private final String mTitle;
    private String mTimer;
    ListItemHeaderBinding mBinding;

    public HeaderAdapter(String title, String timer) {
        mTitle = title;
        mTimer = timer;
    }

    public void updateTime(String countDown) {
        mTimer = countDown;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mBinding = ListItemHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HeaderAdapter.ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mTitle, mTimer);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.list_item_header;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ListItemHeaderBinding mBinding;

        public ViewHolder(@NonNull ListItemHeaderBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(String title, String timer) {
            TextView headerTitle = mBinding.header;
            headerTitle.setText(title);

            if (timer != null) {
                TextView timerView = mBinding.timer;
                timerView.setVisibility(View.VISIBLE);
                timerView.setText("Demo ends in " + timer);
            }
            else {
                mBinding.separator.setVisibility(View.VISIBLE);
            }
        }
    }
}
