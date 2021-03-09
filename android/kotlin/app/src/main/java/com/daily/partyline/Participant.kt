package com.daily.partyline

class Participant(userName: String, Id: String, isModerator: Boolean) {
    private var userName: String
    private val id: String
    private var isModerator: Boolean
    private var isSpeaker: Boolean
    private var isActiveSpeaker: Boolean
    private var isHandRaised: Boolean
    private var isMuted: Boolean
    fun update(userName: String, isModerator: Boolean) {
        this.isModerator = isModerator
        setUserName(userName)
    }

    fun getUserName(): String? {
        return userName
    }

    fun getId(): String? {
        return id
    }

    fun getIsModerator(): Boolean? {
        return isModerator
    }

    fun getIsSpeaker(): Boolean? {
        return isSpeaker
    }

    fun setUserName(userName: String) {
        this.userName = userName//.replace("_", "")
        isSpeaker = isModerator == true || userName.contains(SPEAKER_TAG.toString())
        isHandRaised = userName.contains(RAISE_HAND_TAG.toString())
    }

    fun setId(mId: String?) {
        var mId = mId
        mId = mId
    }

    fun setIsModerator(isModerator: Boolean) {
        this.isModerator = isModerator
    }

    fun setIsSpeaker(isSpeaker: Boolean) {
        setUserName(if (isSpeaker) userName + SPEAKER_TAG else userName.replace(SPEAKER_TAG.toString(), ""))
    }

    fun getIsHandRaised(): Boolean? {
        return isHandRaised
    }

    fun setIsHandRaised(raiseHand: Boolean) {
        setUserName(if (raiseHand) RAISE_HAND_TAG + userName else userName.replace(RAISE_HAND_TAG.toString(), ""))
        isHandRaised = raiseHand
    }

    fun getIsActiveSpeaker(): Boolean? {
        return isActiveSpeaker
    }

    fun setIsActiveSpeaker(isActiveSpeaker: Boolean) {
        this.isActiveSpeaker = isActiveSpeaker
    }

    fun getIsMuted(): Boolean? {
        return isMuted
    }

    fun setIsMuted(isMuted: Boolean) {
        this.isMuted = isMuted
    }

    fun cleanUsername() {
        userName = userName?.replace("_", "")
        userName = userName?.replace(MODERATOR_TAG.toString(), "")
        userName = userName?.replace(LISTENER_TAG.toString(), "")
        userName = userName?.replace(SPEAKER_TAG.toString(), "")
    }

    companion object {
        var myId: String? = null
        val SPEAKER_TAG: String = "SPK"
        val RAISE_HAND_TAG: String = "✋ "
        val LISTENER_TAG: String = "LST"
        val MODERATOR_TAG: String = "MOD"
    }

    init {
        this.userName = userName.replace("_", "")
        id = Id
        isMuted = true
        this.isModerator = isModerator
        isSpeaker = isModerator || userName.contains(other = SPEAKER_TAG.toString())
        isActiveSpeaker = false
        isHandRaised = userName.contains(other = RAISE_HAND_TAG.toString())
    }
}