package com.daily.partyline;

public interface OnItemClickListener {
    void onModeratorMute(String Id);
    void onModeratorChangeRole(String Id);
    void onModeratorMakeModerator(String Id);
    void onModeratorEject(String Id);
}
