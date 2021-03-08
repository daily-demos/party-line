package com.daily.partyline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.daily.partyline.databinding.GridItemParticipantBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> implements Filterable {

    public List<Participant> mParticipants;
    public List<Participant> mFilteredParticipants;
    private final OnItemClickListener mClickListener;
    private Filter mParticipantFilter;

    private GridItemParticipantBinding mBinding;

    public ParticipantsAdapter(OnItemClickListener listener, List<Participant> participants) {
        mClickListener = listener;
        mParticipants = participants;
        mFilteredParticipants = new ArrayList<>(participants);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            mBinding = GridItemParticipantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Participant participantCell = mFilteredParticipants.get(position);

        Participant me = null;
        for (Participant p : mParticipants) {
            if (p.getId().equals(Participant.myId)) {
                me = p;
            }
        }

        // Don't configure the cell yet
        if (me == null) {
            return;
        }

        holder.bind(me, participantCell, mClickListener);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mFilteredParticipants.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.grid_item_participant;
    }

    @Override
    public Filter getFilter() {
        if (mParticipantFilter == null)
            mParticipantFilter = new ParticipantFilter();

        return mParticipantFilter;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        private String mSelectedId;
        private GridItemParticipantBinding mBinding;
        private OnItemClickListener mClickListener;

        public ViewHolder(@NonNull GridItemParticipantBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(Participant me, Participant participantCell, OnItemClickListener clickListener) {
            mClickListener = clickListener;
            MaterialCardView cardView = mBinding.card;

            if (participantCell.getIsSpeaker()) {
                if (!participantCell.getIsMuted()) {
                    mBinding.cellBackground.setBackgroundColor(mBinding.getRoot().getResources().getColor(R.color.teal, null));
                }
                else {
                    mBinding.cellBackground.setBackgroundColor(mBinding.getRoot().getResources().getColor(R.color.gray_default, null));
                }
                mBinding.micOn.setVisibility(participantCell.getIsMuted() ? View.GONE : View.VISIBLE);
                mBinding.micOff.setVisibility(participantCell.getIsMuted() ? View.VISIBLE : View.GONE);
            }
            else {
                mBinding.cellBackground.setBackgroundColor(mBinding.getRoot().getResources().getColor(R.color.gray_default, null));
                mBinding.micOn.setVisibility(View.GONE);
                mBinding.micOff.setVisibility(View.GONE);
            }

            if (participantCell.getIsActiveSpeaker()) {
                cardView.setStrokeWidth(2);
            }
            else {
                cardView.setStrokeWidth(0);
            }

            String[] strings = participantCell.getUserName()
                    .replace("_", "")
                    .replace(Participant.SPEAKER_TAG, "")
                    .replace(Participant.RAISE_HAND_TAG, "")
                    .replace(Participant.LISTENER_TAG, "")
                    .replace(Participant.MODERATOR_TAG, "")
                    .trim()
                    .split(" ");//i18n
            String shortName = "";
            if (strings.length == 0) {
                shortName = participantCell.getUserName();
            }
            else if (strings.length == 1) {
                shortName = strings[0].substring(0, 1);
            } else if (strings.length == 2) {
                shortName = strings[0].substring(0, 1) + strings[1].substring(0, 1);
            }

            mBinding.userInitials.setText(participantCell.getIsHandRaised() ? Participant.RAISE_HAND_TAG + shortName : shortName);
            TextView userName = mBinding.userName;
            userName.setText(strings[0]);

            TextView modaratorAccessory = mBinding.accessoryMod;

            if (participantCell.getIsModerator()) {
                modaratorAccessory.setVisibility(View.VISIBLE);
            }
            else {
                modaratorAccessory.setVisibility(View.INVISIBLE);
            }

            // Mod Controls
            if (me.getIsModerator()) {

                if (!me.getId().equals(participantCell.getId())) {

                    // hack to provide non-contextual popup menu with context
                    mSelectedId = participantCell.getId();

                    mBinding.micOn.setOnClickListener(v -> {
                        mClickListener.onModeratorMute(mSelectedId);
                    });

                    mBinding.modMenu.setVisibility(View.VISIBLE);
                    mBinding.modMenu.setOnClickListener(v -> {
                        showPopup(v, !participantCell.getIsModerator() && !participantCell.getIsSpeaker(), participantCell.getIsModerator(), participantCell.getIsSpeaker());
                    });

                }
            }
        }

        public void showPopup(View v, Boolean isListener, Boolean isModerator, Boolean isSpeaker) {
            PopupMenu popup = new PopupMenu(v.getContext(), v);

            if (isListener) {
                popup.getMenu().add(1, R.id.change_role, 1, "Promote");
                popup.getMenu().add(1, R.id.make_mod, 2, "Make moderator");
                popup.getMenu().add(1, R.id.eject, 3, "Eject");
            }
            if (isSpeaker && !isModerator) {
                popup.getMenu().add(1, R.id.change_role, 1, "Demote");
                popup.getMenu().add(1, R.id.make_mod, 2, "Make moderator");
                popup.getMenu().add(1, R.id.eject, 3,"Eject");
            }
            if (isModerator){
                popup.getMenu().add(1, R.id.eject, 1,"Eject");
            }

            popup.setOnMenuItemClickListener(this);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.change_role:
                    mClickListener.onModeratorChangeRole(mSelectedId);
                    return true;
                case R.id.make_mod:
                    mClickListener.onModeratorMakeModerator(mSelectedId);
                    return true;
                case R.id.eject:
                    mClickListener.onModeratorEject(mSelectedId);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class ParticipantFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Participant> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {

                for (Participant p : mParticipants) {
                    if (!p.getIsSpeaker() && !p.getIsModerator()) {

                        filteredList.add(p);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
            }
            else {
                for (Participant p : mParticipants) {
                    if (p.getIsSpeaker() || p.getIsModerator()) {
                        filteredList.add(p);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredParticipants.clear();
            mFilteredParticipants.addAll((List<Participant>) results.values);
            notifyDataSetChanged();
        }
    }
}