package com.daily.partyline

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.daily.partyline.databinding.GridItemParticipantBinding
import java.util.*

class ParticipantsAdapter(private val clickListener: OnItemClickListener, var participants: MutableList<Participant?>) : RecyclerView.Adapter<ParticipantsAdapter.ViewHolder?>(), Filterable {
    var filteredParticipants: MutableList<Participant>
    private var participantFilter: Filter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GridItemParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participantCell: Participant = filteredParticipants.get(position)
        var me: Participant? = null
        for (p in participants) {
            if (p?.getId() == Participant.Companion.myId) {
                me = p
            }
        }

        // Don't configure the cell yet
        if (me == null) {
            return
        }
        holder.bind(me, participantCell, clickListener)
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getItemCount(): Int {
        return filteredParticipants.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.grid_item_participant
    }

    override fun getFilter(): Filter? {
        if (participantFilter == null) participantFilter = ParticipantFilter()
        return participantFilter
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(var binding: GridItemParticipantBinding) : RecyclerView.ViewHolder(binding.root), PopupMenu.OnMenuItemClickListener {
        private var selectedId: String? = null

        private lateinit var listener: OnItemClickListener
        fun bind(me: Participant?, participantCell: Participant, clickListener: OnItemClickListener) {
            listener = clickListener
            val cardView = binding.card
            if (participantCell.getIsSpeaker() == true) {
                if (participantCell.getIsMuted() == false) {
                    binding.cellBackground.setBackgroundColor(binding.getRoot().resources.getColor(R.color.teal, null))
                } else {
                    binding.cellBackground.setBackgroundColor(binding.getRoot().resources.getColor(R.color.gray_default, null))
                }
                binding.micOn.visibility = if (participantCell.getIsMuted() == true) View.GONE else View.VISIBLE
                binding.micOff.visibility = if (participantCell.getIsMuted() == true) View.VISIBLE else View.GONE
            } else {
                binding.cellBackground.setBackgroundColor(binding.getRoot().resources.getColor(R.color.gray_default, null))
                binding.micOn.visibility = View.GONE
                binding.micOff.visibility = View.GONE
            }
            if (participantCell.getIsActiveSpeaker() == true) {
                cardView.strokeWidth = 2
            } else {
                cardView.strokeWidth = 0
            }
            val strings: Array<String?>? = participantCell.getUserName()
                    ?.replace("_", "")
                    ?.replace(Participant.SPEAKER_TAG, "")
                    ?.replace(Participant.RAISE_HAND_TAG, "")
                    ?.replace(Participant.LISTENER_TAG, "")
                    ?.replace(Participant.MODERATOR_TAG, "")
                    ?.trim { it <= ' ' }
                    ?.split(" ")
                    ?.toTypedArray() //i18n
            var shortName: String? = ""
            if (strings?.size == 0) {
                shortName = participantCell.getUserName()
            } else if (strings?.size == 1) {
                shortName = strings.get(0)?.substring(0, 1)
            } else if (strings?.size == 2) {
                shortName = strings.get(0)?.substring(0, 1) + strings.get(1)?.substring(0, 1)
            }
            binding.userInitials.text = if (participantCell.getIsHandRaised() == true) Participant.RAISE_HAND_TAG + shortName else shortName
            val userName: TextView = binding.userName
            userName.text = strings?.get(0)
            val modaratorAccessory: TextView = binding.accessoryMod
            if (participantCell.getIsModerator() == true) {
                modaratorAccessory.visibility = View.VISIBLE
            } else {
                modaratorAccessory.visibility = View.INVISIBLE
            }

            // Mod Controls
            if (me?.getIsModerator() == true) {
                if (me.getId() != participantCell.getId()) {

                    // hack to provide non-contextual popup menu with context
                    selectedId = participantCell.getId()
                    binding.micOn.setOnClickListener { v: View? -> listener?.onModeratorMute(selectedId) }
                    binding.modMenu.visibility = View.VISIBLE
                    binding.modMenu.setOnClickListener { v: View -> showPopup(v, participantCell.getIsModerator() == false && participantCell.getIsSpeaker() == false, participantCell.getIsModerator() == true, participantCell.getIsSpeaker() == true) }
                }
            }
        }

        fun showPopup(v: View, isListener: Boolean, isModerator: Boolean, isSpeaker: Boolean) {
            val popup = PopupMenu(v.getContext(), v)
            if (isListener) {
                popup.menu.add(1, R.id.change_role, 1, "Promote")
                popup.menu.add(1, R.id.make_mod, 2, "Make moderator")
                popup.menu.add(1, R.id.eject, 3, "Eject")
            }
            if (isSpeaker && !isModerator) {
                popup.menu.add(1, R.id.change_role, 1, "Demote")
                popup.menu.add(1, R.id.make_mod, 2, "Make moderator")
                popup.menu.add(1, R.id.eject, 3, "Eject")
            }
            if (isModerator) {
                popup.menu.add(1, R.id.eject, 1, "Eject")
            }
            popup.setOnMenuItemClickListener(this)
            popup.show()
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.getItemId()) {
                R.id.change_role -> {
                    listener.onModeratorChangeRole(selectedId)
                    true
                }
                R.id.make_mod -> {
                    listener.onModeratorMakeModerator(selectedId)
                    true
                }
                R.id.eject -> {
                    listener.onModeratorEject(selectedId)
                    true
                }
                else -> false
            }
        }

    }

    private inner class ParticipantFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            val results = FilterResults()
            val filteredList: MutableList<Participant?> = ArrayList()
            if (constraint == null || constraint.length == 0) {
                for (p in participants) {
                    if (p?.getIsSpeaker() == false && p?.getIsModerator() == false) {
                        filteredList.add(p)
                    }
                }
                results.values = filteredList
                results.count = filteredList.size
            } else {
                for (p in participants) {
                    if (p?.getIsSpeaker() == true || p?.getIsModerator() == true) {
                        filteredList.add(p)
                    }
                }
                results.values = filteredList
                results.count = filteredList.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredParticipants.clear()
            filteredParticipants.addAll(results?.values as MutableList<Participant>)
            notifyDataSetChanged()
        }
    }

    init {
        filteredParticipants = ArrayList(participants)
    }
}