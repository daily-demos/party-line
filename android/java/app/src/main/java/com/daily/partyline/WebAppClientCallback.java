package com.daily.partyline;

public interface WebAppClientCallback {

    void setRoomName(String roomName);
    void onJoinedMeeting();
    void onTick(String timer);
    void onDataChanged();
    void onForceEject();
    void onError();
    void onRoleChanged();
    void onEndCall();
    void onAudioStateChanged(Boolean isMuted);
}
