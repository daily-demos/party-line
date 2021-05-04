package com.daily.partyline

import android.util.Log
import android.webkit.*
import java.util.*

class WebAppClient {
    var webView: WebView? = null
    var callback: WebAppClientCallback? = null
    var roomName: String? = null
    var userName: String? = null
    private var participants: MutableList<Participant?>? = null

    fun bind(webview: WebView?, roomName: String?, userName: String?, participants: MutableList<Participant?>) {
        webView = webview
        this.roomName = roomName
        this.userName = userName
        this.participants = participants
        loadWebView()
    }

    fun setListener(callback: WebAppClientCallback?) {
        this.callback = callback
    }

    // Interface
    // CHANGE roomURL TO YOUR DAILY DOMAIN
    // EX: https://myaccount.daily.co/

    private fun joinRoom() {
        webView?.evaluateJavascript(
                "userName='" + userName + "_" + Participant.LISTENER_TAG + "';" +
                        "roomUrl='https://devrel.daily.co/" + roomName + "';" +
                        "(async() => {" +
                        "roomName = '" + roomName + "';" +
                        "await joinRoom();" +
                        "roomName = (await call.room()).name;" +
                        "})();", null)
    }

    private fun createAndJoinRoom() {
        webView?.evaluateJavascript(
                "userName='" + userName + "_" + Participant.MODERATOR_TAG + "';" +
                        "(async() => {" +
                        "let roomInfo = await createRoom();" +
                        "roomUrl = roomInfo.roomUrl;" +
                        "roomName = roomInfo.roomName;" +
                        "if (roomUrl) {" +
                        "await joinRoom({ moderator: true });" +
                        "}" +
                        "})();", null)
    }

    fun toggleMic() {
        webView?.evaluateJavascript("toggleMic();", null)
    }

    fun leave() {
        webView?.evaluateJavascript("(async () => { await call.leave(); })();", null)
    }

    fun muteParticipant(Id: String?) {
        webView?.evaluateJavascript(
                "call.updateParticipant('$Id', { setAudio: false });", null)
    }

    fun changeRole(Id: String?) {
        webView?.evaluateJavascript("call.sendAppMessage({ msg: " + (if (getParticipant(Id)?.getIsSpeaker() == true) "MSG_MAKE_LISTENER" else "MSG_MAKE_SPEAKER") + " }, '" + Id + "');", null)
    }

    fun makeModerator(Id: String?) {
        getParticipant(Id)?.cleanUsername()
        webView?.evaluateJavascript(
                "call.sendAppMessage({ userName: '" + getParticipant(Id)?.getUserName() + "_" + Participant.MODERATOR_TAG + "', msg: MSG_MAKE_MODERATOR }, '" + Id + "');", null)
    }

    fun eject(Id: String?) {
        webView?.evaluateJavascript(
                "call.updateParticipant('" + Id + "', { eject: true });" +
                        "call.sendAppMessage({ msg: MSG_FORCE_EJECT }, '" + Id + "');", null)
    }

    fun raiseHand() {
        val me = getParticipant(Participant.myId)
        me?.setIsHandRaised(me.getIsHandRaised() == false)
        webView?.evaluateJavascript(
                "call.setUserName('" + me?.getUserName() + "');",
                null)
    }

    // Setup
    private fun loadWebView() {
        webView?.addJavascriptInterface(WebAppInterface(), JS_INTERFACE_NAME)
        val settings = webView?.getSettings()

        /*
         * Using setJavaScriptEnabled can introduce
         * XSS vulnerabilities into your application,
         * review carefully
         */
        settings?.javaScriptEnabled = true
        settings?.mediaPlaybackRequiresUserGesture = false

        /*
         * Let the current WebView consume links vs. browser
         */webView?.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (roomName != null) {
                    joinRoom()
                } else if (roomName == null) {
                    createAndJoinRoom()
                }
            }
        })
        webView?.loadUrl("file:///android_asset/audio-single-file.html")
        webView?.setWebChromeClient(object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.getResources())
            }
        })
    }

    //region Utilities
    fun containsKey(Id: String?): Boolean? {
        for (p in participants!!) {
            if (p?.getId().equals(Id, ignoreCase = true)) return true
        }
        return false
    }

    fun getParticipant(Id: String?): Participant? {
        participants?.let {
            synchronized(it) {
                for (p in participants!!) {
                    if (p?.getId().equals(Id, ignoreCase = true)) return p
                }
            }
        }
        return null
    }

    fun getActiveSpeaker(): Participant? {
        participants?.let {
            synchronized(it) {
                for (p in participants!!) {
                    if (p?.getIsActiveSpeaker() == true) return p
                }
            }
        }
        return null
    }

    //endregion Utilities
    // Inner class
    internal inner class WebAppInterface {
        @JavascriptInterface
        fun updateState(roomName: String?) {
            callback?.setRoomName(roomName)
        }

        @JavascriptInterface
        fun updateTimer(timer: String?) {
            Log.e(TAG, timer)
            callback?.onTick(timer)
        }

        @JavascriptInterface
        fun joinedMeeting(Id: String?) {
            Participant.Companion.myId = Id
            callback?.onJoinedMeeting()
        }

        @JavascriptInterface
        fun handleParticipantJoinedOrUpdated(userName: String, Id: String, isModerator: String?) {
            val participant: Participant?
            if (containsKey(Id) == false) {
                // Joined
                participant = Participant(userName, Id, java.lang.Boolean.parseBoolean(isModerator))
                participants?.let {
                    synchronized(it) {
                        participants?.add(participant)
                    }
                }
            } else {
                // Updated
                participant = getParticipant(Id)
                participant?.update(userName, java.lang.Boolean.parseBoolean(isModerator))
            }
            Log.e(TAG, "new/updated user " + participant?.getUserName())
            callback?.onDataChanged()
        }

        @JavascriptInterface
        fun handleMute(isMuted: String?) {
            getParticipant(Participant.myId)?.setIsMuted(java.lang.Boolean.parseBoolean(isMuted))
            callback?.onAudioStateChanged(java.lang.Boolean.parseBoolean(isMuted))
            callback?.onDataChanged()
        }

        @JavascriptInterface
        fun handlePromote(userName: String, Id: String) {
            val participant = getParticipant(Id)
            participant?.update(userName, participant.getIsModerator() == true)
            callback?.onDataChanged()
            callback?.onRoleChanged()
        }

        @JavascriptInterface
        fun handleParticipantLeft(Id: String?) {
            participants?.let {
                synchronized(it) {
                    participants?.remove(getParticipant(Id))
                }
                callback?.onDataChanged()
            }
        }

        @JavascriptInterface
        fun handleForceEject() {
            callback?.onForceEject()
        }

        @JavascriptInterface
        fun endCall() {
            callback?.onEndCall();
        }

        @JavascriptInterface
        fun error() {
            callback?.onError()
        }

        @JavascriptInterface
        fun handleParticpantAudioChange(Id: String?, isMuted: String?) {
            getParticipant(Id)?.setIsMuted(java.lang.Boolean.parseBoolean(isMuted))
            callback?.onDataChanged()
        }

        @JavascriptInterface
        fun handleActiveSpeakerChange(Id: String?) {
            getActiveSpeaker()?.setIsActiveSpeaker(false)
            getParticipant(Id)?.setIsActiveSpeaker(true)
            callback?.onDataChanged()
        }
    }

    companion object {
        private val JS_INTERFACE_NAME: String? = "Android"
        private val TAG: String? = "WebAppClient"
    }
}