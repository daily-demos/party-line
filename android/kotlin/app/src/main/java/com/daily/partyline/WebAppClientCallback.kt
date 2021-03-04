package com.daily.partyline

interface WebAppClientCallback {
    open fun setRoomName(roomName: String?)
    open fun onJoinedMeeting()
    open fun onTick(timer: String?)
    open fun onDataChanged()
    open fun onForceEject()
    open fun onRoleChanged()
    open fun onAudioStateChanged(isMuted: Boolean)
}