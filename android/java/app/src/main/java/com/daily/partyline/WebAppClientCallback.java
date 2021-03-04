package com.daily.partyline;

public interface WebAppClientCallback {

    void setRoomName(String roomName);
    void onJoinedMeeting();
    void onTick(String timer);
    void onDataChanged();
    void onForceEject();
    void onRoleChanged();
    void onAudioStateChanged(Boolean isMuted);
}
