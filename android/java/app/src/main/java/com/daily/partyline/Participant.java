package com.daily.partyline;

public class Participant {

    public static String myId;
    public static final String SPEAKER_TAG = "SPK";
    public static final String RAISE_HAND_TAG = "✋ ";
    public static final String LISTENER_TAG = "LST";
    public static final String MODERATOR_TAG = "MOD";
    private String mUserName;
    private String mId;
    private Boolean mIsModerator;
    private Boolean mIsSpeaker;
    private Boolean mIsActiveSpeaker;
    private Boolean mIsHandRaised;
    private Boolean mIsMuted;

    public Participant(String userName, String Id, Boolean isModerator) {
        mUserName = userName.replace("_", "");
        mId = Id;
        mIsMuted = true;
        mIsModerator = isModerator;
        mIsSpeaker = isModerator || userName.contains(SPEAKER_TAG);
        mIsActiveSpeaker = false;
        mIsHandRaised = userName.contains(RAISE_HAND_TAG);
    }

    public void update(String userName, Boolean isModerator) {
        mIsModerator = isModerator;
        setUserName(userName);
    }

    public String getUserName() {
        return mUserName;
    }

    public String getId() {
        return mId;
    }

    public Boolean getIsModerator() {
        return mIsModerator;
    }

    public Boolean getIsSpeaker() {
        return mIsSpeaker;
    }

    public void setUserName(String userName) {
        mUserName = userName;//.replace("_", "");
        mIsSpeaker = mIsModerator || userName.contains(SPEAKER_TAG);
        mIsHandRaised = userName.contains(RAISE_HAND_TAG);
    }

    public void setId(String mId) {
        mId = mId;
    }

    public void setIsModerator(Boolean isModerator) {
        mIsModerator = isModerator;
    }

    public void setIsSpeaker(Boolean isSpeaker) {
        setUserName(isSpeaker ? mUserName.concat(SPEAKER_TAG) : mUserName.replace(SPEAKER_TAG, ""));
    }

    public Boolean getIsHandRaised() {
        return mIsHandRaised;
    }

    public void setIsHandRaised(Boolean raiseHand) {
        setUserName(raiseHand ? RAISE_HAND_TAG.concat(mUserName) : mUserName.replace(RAISE_HAND_TAG, ""));
        mIsHandRaised = raiseHand;
    }

    public Boolean getIsActiveSpeaker() {
        return mIsActiveSpeaker;
    }

    public void setIsActiveSpeaker(Boolean isActiveSpeaker) {
        mIsActiveSpeaker = isActiveSpeaker;
    }

    public Boolean getIsMuted() {
        return mIsMuted;
    }

    public void setIsMuted(Boolean isMuted) {
        mIsMuted = isMuted;
    }

    public void cleanUsername() {
        mUserName = mUserName.replace("_", "");
        mUserName = mUserName.replace(Participant.MODERATOR_TAG, "");
        mUserName = mUserName.replace(Participant.LISTENER_TAG, "");
        mUserName = mUserName.replace(Participant.SPEAKER_TAG, "");
    }
}
