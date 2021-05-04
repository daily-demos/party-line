package com.daily.partyline;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;
import java.util.Objects;

public class WebAppClient {

    private static final String JS_INTERFACE_NAME = "Android";
    private static final String TAG = "WebAppClient";

    WebView mWebView;
    WebAppClientCallback mCallback;
    String mRoomName, mUserName;
    private List<Participant> mParticipants;

    public void bind(WebView webview, String roomName, String userName, List<Participant> participants) {
        mWebView = webview;
        mRoomName = roomName;
        mUserName = userName;
        mParticipants = participants;
        loadWebView();
    }

    public void setListener(WebAppClientCallback callback) {
        mCallback = callback;
    }

    // Interface
    // CHANGE roomUrl TO YOUR DAILY DOMAIN
    // EX: https://myaccount.daily.co/
    
    private void joinRoom() {
        mWebView.evaluateJavascript(
                "userName='" + mUserName + "_" + Participant.LISTENER_TAG + "';" +
                        "roomUrl='https://devrel.daily.co/" + mRoomName + "';" +
                        "(async() => {" +
                        "roomName = '" + mRoomName + "';" +
                        "await joinRoom();" +
                        "roomName = (await call.room()).name;" +
                        "})();", null);
    }

    private void createAndJoinRoom() {
        mWebView.evaluateJavascript(
                "userName='" + mUserName + "_" + Participant.MODERATOR_TAG + "';" +
                        "(async() => {" +
                        "let roomInfo = await createRoom();" +
                        "roomUrl = roomInfo.roomUrl;" +
                        "roomName = roomInfo.roomName;" +
                        "if (roomUrl) {" +
                        "await joinRoom({ moderator: true });" +
                        "}" +
                        "})();", null);
    }

    public void toggleMic() {
        mWebView.evaluateJavascript("toggleMic();", null);
    }

    public void leave() {
        mWebView.evaluateJavascript("(async () => { await call.leave(); })();", null);
    }

    public void muteParticipant(String Id) {
        mWebView.evaluateJavascript(
                "call.updateParticipant('" + Id + "', { setAudio: false });", null);
    }

    public void changeRole(String Id) {
        mWebView.evaluateJavascript("call.sendAppMessage({ msg: " + (getParticipant(Id).getIsSpeaker() ? "MSG_MAKE_LISTENER" : "MSG_MAKE_SPEAKER") + " }, '" + Id + "');", null);
    }

    public void makeModerator(String Id) {
        Participant participant = getParticipant(Id);
        if (participant != null) {
            participant.cleanUsername();
            mWebView.evaluateJavascript(
                    "call.sendAppMessage({ userName: '" + participant.getUserName() + "_" + Participant.MODERATOR_TAG + "', msg: MSG_MAKE_MODERATOR }, '" + Id + "');", null);
        }

    }

    public void eject(String Id) {
        mWebView.evaluateJavascript(
                "call.updateParticipant('" + Id + "', { eject: true });" +
                        "call.sendAppMessage({ msg: MSG_FORCE_EJECT }, '" + Id + "');", null);
    }

    public void raiseHand() {
        Participant me = getParticipant(Participant.myId);
        if (me != null) {
            me.setIsHandRaised(!me.getIsHandRaised());
            mWebView.evaluateJavascript(
                    "call.setUserName('" + me.getUserName() +"');",
                    null);
        }
    }

    // Setup

    private void loadWebView() {

        mWebView.addJavascriptInterface(new WebAppInterface(), JS_INTERFACE_NAME);

        WebSettings settings = mWebView.getSettings();

        /*
         * Using setJavaScriptEnabled can introduce
         * XSS vulnerabilities into your application,
         * review carefully
         */
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        /*
         * Let the current WebView consume links vs. browser
         */
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                if (mRoomName != null) {
                    joinRoom();
                }
                else if (mRoomName == null) {
                    createAndJoinRoom();
                }
            }
        });

        mWebView.loadUrl("file:///android_asset/audio-single-file.html");

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
    }

    //region Utilities
    Boolean containsKey(String Id) {
        for (Participant p : mParticipants) {
            if (p.getId().equalsIgnoreCase(Id))
                return true;
        }

        return false;
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

    Participant getActiveSpeaker() {
        synchronized(mParticipants) {
            for (Participant p : mParticipants) {
                if (p.getIsActiveSpeaker())
                    return p;
            }
        }

        return null;
    }
    //endregion Utilities

    // Inner class
    final class WebAppInterface {

        @JavascriptInterface
        public void updateState(String roomName) {
            mCallback.setRoomName(roomName);
        }

        @JavascriptInterface
        public void updateTimer(String timer) {
            Log.e(TAG, timer);
            mCallback.onTick(timer);
        }

        @JavascriptInterface
        public void joinedMeeting(String Id) {
            Participant.myId = Id;
            mCallback.onJoinedMeeting();
        }

        @JavascriptInterface
        public void handleParticipantJoinedOrUpdated(String userName, String Id, String isModerator) {
            Participant participant;
            if (!containsKey(Id)) {
                // Joined
                participant = new Participant(userName, Id, Boolean.parseBoolean(isModerator));
                synchronized(mParticipants) {
                    mParticipants.add(participant);
                }
            } else {
                // Updated
                participant = getParticipant(Id);
                if (participant == null) {
                    return;
                }
                participant.update(userName, Boolean.parseBoolean(isModerator));
            }
            mCallback.onDataChanged();
        }

        @JavascriptInterface
        public void handleMute(String isMuted) {
            Participant me = getParticipant(Participant.myId);
            if (me != null) {
                me.setIsMuted(Boolean.parseBoolean(isMuted));
                mCallback.onAudioStateChanged(Boolean.parseBoolean(isMuted));
                mCallback.onDataChanged();
            }
        }

        @JavascriptInterface
        public void handlePromote(String userName, String Id) {
            Participant participant = getParticipant(Id);
            if (participant != null) {
                participant.update(userName, participant.getIsModerator());
                mCallback.onDataChanged();
                mCallback.onRoleChanged();
            }
        }

        @JavascriptInterface
        public void handleParticipantLeft(String Id) {
            synchronized(mParticipants) {
                mParticipants.remove(getParticipant(Id));
            }
            mCallback.onDataChanged();
        }

        @JavascriptInterface
        public void handleForceEject() {
            mCallback.onForceEject();
        }

        @JavascriptInterface
        public void endCall() {
            mCallback.onEndCall();
        }

        @JavascriptInterface
        public void error() {
            mCallback.onError();
        }

        @JavascriptInterface
        public void handleParticpantAudioChange(String Id, String isMuted) {
            Participant participant = getParticipant(Id);
            if (participant != null) {
                participant.setIsMuted(Boolean.parseBoolean(isMuted));
                mCallback.onDataChanged();
            }
        }

        @JavascriptInterface
        public void handleActiveSpeakerChange(String Id) {
            Participant activeSpeaker = getActiveSpeaker();
            if (activeSpeaker != null) {
                activeSpeaker.setIsActiveSpeaker(false);
            }
            Participant participant = getParticipant(Id);
            if (participant != null) {
                participant.setIsActiveSpeaker(true);
            }
            mCallback.onDataChanged();
        }
    }
}
