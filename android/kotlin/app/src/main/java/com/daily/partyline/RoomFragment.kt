package com.daily.partyline

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.EmojiCompat.InitCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ConcatAdapter
import com.daily.partyline.databinding.FragmentRoomBinding
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.util.*
import kotlin.collections.ArrayList

class RoomFragment : Fragment(), WebAppClientCallback {
    private lateinit var binding: FragmentRoomBinding
    private val permissionHandler: PermissionHandler? = object : PermissionHandler() {
        override fun onGranted() {
            client?.bind(binding.webview, roomName, userName, participants)
        }
    }
    private var listenersAdapter: ParticipantsAdapter? = null
    private var speakersAdapter: ParticipantsAdapter? = null
    private var headerAdapter: HeaderAdapter? = null
    private var footerAdapter: FooterAdapter? = null
    private lateinit var participants: MutableList<Participant?>
    private var userName: String? = null
    private var roomName: String? = null
    private var client: WebAppClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEmojiCompat()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    // Post view initialization logic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.progressbar.visibility = View.VISIBLE
        client = WebAppClient()
        client?.setListener(this@RoomFragment)

        userName = RoomFragmentArgs.fromBundle(requireArguments()).getUserName()
        roomName = RoomFragmentArgs.fromBundle(requireArguments()).getRoomUrl()

        overrideGestureNavigation()

        binding.toggleButton.setOnClickListener { sender: View? -> client?.toggleMic() }
        binding.leaveButton.setOnClickListener { sender: View? ->
            client?.leave()
            val navController = Navigation.findNavController(view)
            navController.popBackStack()
        }

        binding.raiseButton.setOnClickListener { sender: View? ->
            client?.raiseHand()
            notifyUI()
        }

        val clickListener: OnItemClickListener = object : OnItemClickListener {
            override fun onModeratorMute(Id: String?) {
                client?.muteParticipant(Id)
            }

            override fun onModeratorChangeRole(Id: String?) {
                if (getParticipant(Id)?.getIsModerator() == true) {
                    return
                }
                client?.changeRole(Id)
            }

            override fun onModeratorMakeModerator(Id: String?) {
                client?.makeModerator(Id)
            }

            override fun onModeratorEject(Id: String?) {
                client?.eject(Id)
            }
        }

        participants = Collections.synchronizedList(ArrayList())
        speakersAdapter = ParticipantsAdapter(clickListener, participants)
        listenersAdapter = ParticipantsAdapter(clickListener, participants)
        headerAdapter = HeaderAdapter("Speakers", "00:00")
        footerAdapter = FooterAdapter(roomName)

        val concatAdapter = ConcatAdapter(
                headerAdapter,
                GridConcatAdapter(speakersAdapter),
                HeaderAdapter("Listeners", null),
                GridConcatAdapter(listenersAdapter),
                footerAdapter)

        //  Allow views to be recycled accross adapters
        val configBuilder = ConcatAdapter.Config.Builder()
        configBuilder.setIsolateViewTypes(false)
        binding.parentRecyclerView.adapter = concatAdapter
        checkPermissions()
    }

    override fun onDestroy() {
        client?.leave()
        binding.webview.removeJavascriptInterface(JS_INTERFACE_NAME)
        participants.clear()
        participants = mutableListOf(null)
        client = null
        binding.webview.getSettings().setJavaScriptEnabled(false)
        super.onDestroy()
    }

    private fun initEmojiCompat() {
        // Use the bundled font for EmojiCompat
        val config: EmojiCompat.Config = BundledEmojiCompatConfig(requireActivity())
        config.setReplaceAll(true)
                .registerInitCallback(object : InitCallback() {
                    override fun onInitialized() {
                        Log.i(TAG, "EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Log.e(TAG, "EmojiCompat initialization failed", throwable)
                    }
                })
        EmojiCompat.init(config)
    }

    private fun overrideGestureNavigation() {
        // This callback will only be called when the fragment is at least Started
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showControls() {
        val me = getParticipant(Participant.myId)
        if (me?.getIsModerator() == true) {
            binding.toggleButton.visibility = View.VISIBLE
            binding.raiseButton.visibility = View.GONE
            binding.leaveButton.visibility = View.VISIBLE
        }
        if (me?.getIsSpeaker() == true) {
            binding.toggleButton.visibility = View.VISIBLE
            binding.raiseButton.visibility = View.GONE
            binding.leaveButton.visibility = View.VISIBLE
        }
        if (me?.getIsSpeaker() == false && me?.getIsModerator() == false) {
            binding.raiseButton.visibility = View.VISIBLE
            binding.leaveButton.visibility = View.VISIBLE
            binding.toggleButton.visibility = View.GONE
        }
    }

    private fun toggleAudioUI(isMuted: Boolean) {
        val me = getParticipant(Participant.myId)
        if (me?.getIsModerator() == true || me?.getIsSpeaker() == true) {
            binding.toggleButton.setCompoundDrawablesWithIntrinsicBounds(if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on, 0, 0, 0)
            binding.toggleButton.text = if (isMuted) "Unmute mic" else "Mute mic"
        }
    }

    fun getParticipant(Id: String?): Participant? {
        synchronized(participants) {
            for (p in participants) {
                if (p?.getId().equals(Id, ignoreCase = true)) return p
            }
        }
        return null
    }

    private fun notifyUI() {
        requireActivity().runOnUiThread {

            // The filter's publishResults() will call notifyDataSetChanged() for us
            speakersAdapter?.getFilter()?.filter(Participant.SPEAKER_TAG)
            listenersAdapter?.getFilter()?.filter(null)
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf<String?>(
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO)
        val rationale = "Allow microphone access"
        val options = Permissions.Options().setRationaleDialogTitle("Info").setSettingsDialogTitle("Warning")
        Permissions.check(activity, permissions, rationale, options, permissionHandler)
    }

    override fun setRoomName(roomName: String?) {
        Log.v(TAG, roomName)
        this.roomName = roomName
        requireActivity().runOnUiThread { footerAdapter?.setRoomUrl(this.roomName) }
    }

    override fun onJoinedMeeting() {
        requireActivity().runOnUiThread {
            binding.parentRecyclerView.visibility = View.VISIBLE
            binding.progressbar.visibility = View.INVISIBLE
            showControls()
        }
    }

    override fun onRoleChanged() {
        requireActivity().runOnUiThread { showControls() }
    }

    override fun onTick(timer: String?) {
        requireActivity().runOnUiThread { headerAdapter?.updateTime(timer) }
    }

    override fun onDataChanged() {
        notifyUI()
    }

    override fun onForceEject() {
        requireActivity().runOnUiThread {
            client?.leave()
            binding.progressbar.setVisibility(View.INVISIBLE);
            val navController = Navigation.findNavController(binding.getRoot())
            navController.popBackStack()
        }
    }

    override fun onError() {
        requireActivity().runOnUiThread {
            val text: CharSequence = "Something went wrong! Make sure the code is correct and the room exists"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(requireActivity(), text, duration)
            toast.show()
            client?.leave()
            binding.progressbar.setVisibility(View.INVISIBLE)
            val navController = Navigation.findNavController(binding.root)
            navController.popBackStack()
        }
    }

    override fun onEndCall() {
        requireActivity().runOnUiThread {
            if (getParticipant(Participant.myId)?.getIsModerator() == true) {
                synchronized(participants) {
                    for (p in participants) {
                        if (p?.getId() != Participant.myId) {
                            client?.eject(p?.getId())
                        }
                    }
                }
            }
            client?.leave()
            binding.progressbar.setVisibility(View.INVISIBLE)
            val navController = Navigation.findNavController(binding.getRoot())
            navController.popBackStack()
        }
    }

    override fun onAudioStateChanged(isMuted: Boolean) {
        requireActivity().runOnUiThread { toggleAudioUI(isMuted) }
    }

    companion object {
        private val TAG: String? = "RoomFragment"
        private val JS_INTERFACE_NAME: String? = "Android"
    }
}