package com.daily.partyline

interface WebAppClientCallback {
    fun setRoomName(roomName: String?)
    fun onJoinedMeeting()
    fun onTick(timer: String?)
    fun onDataChanged()
    fun onForceEject()
    fun onError()
    fun onRoleChanged()
    fun onEndCall()
    fun onAudioStateChanged(isMuted: Boolean)
}