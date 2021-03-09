package com.daily.partyline;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ConcatAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.daily.partyline.databinding.FragmentRoomBinding;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static androidx.emoji.text.EmojiCompat.get;
import static androidx.emoji.text.EmojiCompat.init;

public class RoomFragment extends Fragment implements WebAppClientCallback{

    private static final String TAG = "RoomFragment";
    private FragmentRoomBinding mBinding;

    private static final String JS_INTERFACE_NAME = "Android";

    private final PermissionHandler mPermissionHandler = new PermissionHandler() {
        @Override
        public void onGranted() {
            mClient.bind(mBinding.webview, mRoomName, mUserName, mParticipants);
        }
    };

    private ParticipantsAdapter mListenersAdapter, mSpeakersAdapter;
    private HeaderAdapter mHeaderAdapter;
    private FooterAdapter mFooterAdapter;
    private List<Participant> mParticipants;
    private String mUserName;
    private String mRoomName;

    private WebAppClient mClient;

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initEmojiCompat();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentRoomBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    // Post view initialization logic
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mBinding.progressbar.setVisibility(View.VISIBLE);

        mClient = new WebAppClient();
        mClient.setListener(RoomFragment.this);

        assert getArguments() != null;
        mUserName = RoomFragmentArgs.fromBundle(getArguments()).getUserName();
        mRoomName = RoomFragmentArgs.fromBundle(getArguments()).getRoomUrl();

        overrideGestureNavigation();

        mBinding.toggleButton.setOnClickListener((sender) -> {
            mClient.toggleMic();
        });
        mBinding.leaveButton.setOnClickListener((sender) -> {
            mClient.leave();
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });
        mBinding.raiseButton.setOnClickListener((sender) -> {
            mClient.raiseHand();
            notifyUI();
        });

        OnItemClickListener clickListener = new OnItemClickListener() {
            @Override
            public void onModeratorMute(String Id) {
                mClient.muteParticipant(Id);
            }

            @Override
            public void onModeratorChangeRole(String Id) {
                Participant participant = getParticipant(Id);
                if (participant != null) {
                    if (participant.getIsModerator()) {
                        return;
                    }

                    mClient.changeRole(Id);
                }
            }

            @Override
            public void onModeratorMakeModerator(String Id) {
                mClient.makeModerator(Id);
            }

            @Override
            public void onModeratorEject(String Id) {
                mClient.eject(Id);
            }
        };

        mParticipants = Collections.synchronizedList(new ArrayList());
        mSpeakersAdapter = new ParticipantsAdapter(clickListener, mParticipants);
        mListenersAdapter = new ParticipantsAdapter(clickListener, mParticipants);
        mHeaderAdapter = new HeaderAdapter("Speakers", "00:00");
        mFooterAdapter = new FooterAdapter(mRoomName);

        ConcatAdapter concatAdapter = new ConcatAdapter(
                mHeaderAdapter,
                new GridConcatAdapter(mSpeakersAdapter),
                new HeaderAdapter("Listeners", null),
                new GridConcatAdapter(mListenersAdapter),
                mFooterAdapter);

        //  Allow views to be recycled accross adapters
        ConcatAdapter.Config.Builder configBuilder = new ConcatAdapter.Config.Builder();
        configBuilder.setIsolateViewTypes(false);

        mBinding.parentRecyclerView.setAdapter(concatAdapter);

        checkPermissions();
    }

    @Override
    public void onDestroy() {
        mClient.leave();
        mBinding.webview.removeJavascriptInterface(JS_INTERFACE_NAME);
        mParticipants.clear();
        // mParticipants = null;
        mClient = null;
        mBinding.webview.getSettings().setJavaScriptEnabled(false);
        super.onDestroy();
    }

    private void initEmojiCompat() {
        // Use the bundled font for EmojiCompat
        final EmojiCompat.Config config = new BundledEmojiCompatConfig(requireActivity());

        config.setReplaceAll(true)
                .registerInitCallback(new EmojiCompat.InitCallback() {
                    @Override
                    public void onInitialized() {
                        Log.i(TAG, "EmojiCompat initialized");
                    }

                    @Override
                    public void onFailed(@Nullable Throwable throwable) {
                        Log.e(TAG, "EmojiCompat initialization failed", throwable);
                    }
                });

        init(config);
    }

    private void overrideGestureNavigation() {
        // This callback will only be called when the fragment is at least Started
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void showControls() {

        Participant me = getParticipant(Participant.myId);

        if (me == null) {
            return;
        }

        if (me.getIsModerator()) {
            mBinding.toggleButton.setVisibility(View.VISIBLE);
            mBinding.raiseButton.setVisibility(View.GONE);
            mBinding.leaveButton.setVisibility(View.VISIBLE);
        }
        if (me.getIsSpeaker()) {
            mBinding.toggleButton.setVisibility(View.VISIBLE);
            mBinding.raiseButton.setVisibility(View.GONE);
            mBinding.leaveButton.setVisibility(View.VISIBLE);
        }
        if (!me.getIsSpeaker() && !me.getIsModerator()) {
            mBinding.raiseButton.setVisibility(View.VISIBLE);
            mBinding.leaveButton.setVisibility(View.VISIBLE);
            mBinding.toggleButton.setVisibility(View.GONE);
        }

    }

    private void toggleAudioUI(Boolean isMuted) {
        Participant me = getParticipant(Participant.myId);
        if (me != null) {
            if (me.getIsModerator() || me.getIsSpeaker()) {
                mBinding.toggleButton.setCompoundDrawablesWithIntrinsicBounds(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on, 0, 0, 0);
                mBinding.toggleButton.setText(isMuted ? "Unmute mic" : "Mute mic");
            }
        }
    }

    Participant getParticipant(String Id) {
        synchronized(mParticipants) {
            for (Participant p : mParticipants) {
                if (p.getId().equalsIgnoreCase(Id))
                    return p;
            }
        }

        return null;
    }

    private void notifyUI() {
        requireActivity().runOnUiThread(() -> {
            // The filter's publishResults() will call notifyDataSetChanged() for us
            mSpeakersAdapter.getFilter().filter(Participant.SPEAKER_TAG);
            mListenersAdapter.getFilter().filter(null);
        });
    }

    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
        };
        String rationale = "Allow microphone access";
        Permissions.Options options = new Permissions.Options().setRationaleDialogTitle("Info").setSettingsDialogTitle("Warning");
        Permissions.check(getActivity(), permissions, rationale, options, mPermissionHandler);
    }

    @Override
    public void setRoomName(String roomName) {
        Log.v(TAG, roomName);
        mRoomName = roomName;
        requireActivity().runOnUiThread(() -> {
            mFooterAdapter.setRoomUrl(mRoomName);
        });
    }

    @Override
    public void onJoinedMeeting() {
        requireActivity().runOnUiThread(() -> {
            mBinding.parentRecyclerView.setVisibility(View.VISIBLE);
            mBinding.progressbar.setVisibility(View.INVISIBLE);
            showControls();
        });
    }

    @Override
    public void onRoleChanged() {
        requireActivity().runOnUiThread(() -> {
            showControls();
        });
    }

    @Override
    public void onTick(String timer) {
        requireActivity().runOnUiThread(() -> {
            mHeaderAdapter.updateTime(timer);
        });
    }

    @Override
    public void onDataChanged() {
        notifyUI();
    }

    @Override
    public void onForceEject() {
        requireActivity().runOnUiThread(() -> {
            mClient.leave();
            mBinding.progressbar.setVisibility(View.INVISIBLE);
            NavController navController = Navigation.findNavController(mBinding.getRoot());
            navController.popBackStack();
        });
    }

    @Override
    public void onError() {
        requireActivity().runOnUiThread(() -> {
            CharSequence text = "Something went wrong! Make sure the code is correct and the room exists";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(requireActivity(), text, duration);
            toast.show();

            if (mClient != null) {
                mClient.leave();
            }
            mBinding.progressbar.setVisibility(View.INVISIBLE);

            NavController navController = Navigation.findNavController(mBinding.getRoot());
            navController.popBackStack();
        });
    }

    @Override
    public void onEndCall() {
        requireActivity().runOnUiThread(() -> {
            Participant me = getParticipant(Participant.myId);
            if (me != null && me.getIsModerator()) {
                synchronized(mParticipants) {
                    for (Participant p : mParticipants) {
                        if (!p.getId().equals(Participant.myId)) {
                            if (mClient != null) {
                                mClient.eject(p.getId());
                            }
                        }
                    }
                }
            }

            if (mClient != null) {
                mClient.leave();
            }
            mBinding.progressbar.setVisibility(View.INVISIBLE);

            NavController navController = Navigation.findNavController(mBinding.getRoot());
            navController.popBackStack();
        });
    }

    @Override
    public void onAudioStateChanged(Boolean isMuted) {
        requireActivity().runOnUiThread(() -> {
            toggleAudioUI(isMuted);
        });
    }
}