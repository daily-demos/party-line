package com.daily.partyline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daily.partyline.databinding.ListItemHeaderBinding;
import com.daily.partyline.databinding.ListItemInnerGridBinding;

public class GridConcatAdapter extends RecyclerView.Adapter<GridConcatAdapter.ViewHolder> {

    ParticipantsAdapter mParticipantsAdapter;
    ListItemInnerGridBinding mBinding;

    public GridConcatAdapter(ParticipantsAdapter adapter) {
        mParticipantsAdapter = adapter;
    }

    @NonNull
    @Override
    public GridConcatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mBinding = ListItemInnerGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GridConcatAdapter.ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mParticipantsAdapter);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.list_item_inner_grid;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ListItemInnerGridBinding mBinding;

        public ViewHolder(ListItemInnerGridBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(ParticipantsAdapter adapter) {
            mBinding.childRecyclerView.setAdapter(adapter);
        }
    }
}
