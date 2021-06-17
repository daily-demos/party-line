import React, { useEffect, useMemo, useRef } from "react";
import styled from "styled-components";
import { useCallState } from "../CallProvider";
import { LISTENER, MOD, SPEAKER } from "../App";
import useClickAway from "../useClickAway";
import MicIcon from "./MicIcon";
import MutedIcon from "./MutedIcon";
import MoreIcon from "./MoreIcon";
import Menu from "./Menu";
import theme from "../theme";

const AVATAR_DIMENSION = 80;
const ADMIN_BADGE = "⭐ ";

const initials = (name) =>
  name
    ? name
        .split(" ")
        .map((n) => n.charAt(0))
        .join("")
    : "";

const Participant = ({ participant, local, modCount }) => {
  const {
    getAccountType,
    activeSpeakerId,
    changeAccountType,
    displayName,
    handleMute,
    handleUnmute,
    removeFromCall,
    lowerHand,
    raiseHand,
    leaveCall,
    endCall,
  } = useCallState();
  const audioRef = useRef(null);
  const { ref, isVisible, setIsVisible } = useClickAway(false);

  const name = displayName(participant?.user_name);

  const menuOptions = useMemo(() => {
    const mutedText = participant?.audio ? "Mute" : "Unmute";

    const audioAction = participant?.audio
      ? (id) => handleMute(id)
      : (id) => handleUnmute(id);

    /**
     * Determine what the menu options are based on the account type.
     * Listeners can't unmute but can raise their hand to speaker.
     * Moderators can change the status of others but can't have their
     * own status change to speaker or listener.
     * Moderators cannot unmute but can mute.
     */
    let options = [];

    /**
     * If it's the local particpant's menu:
     *  - Mods can unmute themselves and speakers.
     *  - Speakers can unmute themselves.
     *  - Listeners listen. :)
     */
    if (
      participant?.local &&
      [MOD, SPEAKER].includes(getAccountType(participant?.user_name))
    ) {
      options.push({
        text: mutedText,
        action: () => audioAction(participant),
      });
    }

    /**
     * If it's a remote participant:
     * Mods can only MUTE someone. We don't want
     * people getting unmuted without knowing because
     * it can be a bit invasive 😬
     */
    if (
      !participant?.local &&
      participant?.audio &&
      getAccountType(local?.user_name) === MOD &&
      [MOD, SPEAKER].includes(getAccountType(participant?.user_name))
    ) {
      options.push({
        text: "Mute",
        action: () => handleMute(participant),
      });
    }

    switch (getAccountType(participant?.user_name)) {
      case SPEAKER:
        if (!participant?.local) {
          const o = [
            {
              text: "Make moderator",
              action: () => changeAccountType(participant, MOD),
            },
            {
              text: "Make listener",
              action: () => changeAccountType(participant, LISTENER),
            },
            {
              text: "Remove from call",
              action: () => removeFromCall(participant),
              warning: true,
            },
          ];
          options = [...options, ...o];
        }
        break;
      case LISTENER:
        if (participant?.local) {
          options.push({
            text: participant?.user_name.includes("✋")
              ? "Lower hand"
              : "Raise hand ✋",
            action: participant?.user_name.includes("✋")
              ? () => lowerHand(participant)
              : () => raiseHand(participant),
          });
        } else {
          const o = [
            {
              text: "Make moderator",
              action: () => changeAccountType(participant, MOD),
            },
            {
              text: "Make speaker",
              action: () => changeAccountType(participant, SPEAKER),
            },
            {
              text: "Remove from call",
              action: () => removeFromCall(participant),
              warning: true,
            },
          ];
          options = [...options, ...o];
        }
        break;
      default:
        break;
    }

    /**
     * Let the local participant leave. (There's also
     * a button in the tray.) "Leave" or "Remove" should
     * be the last items
     */
    if (participant?.local) {
      const lastMod =
        modCount < 2 && getAccountType(participant?.user_name) === MOD;
      options.push({
        text: lastMod ? "End call" : "Leave call",
        action: () => (lastMod ? endCall() : leaveCall(participant)),
        warning: true,
      });
    }

    return options;
  }, [
    participant,
    local,
    getAccountType,
    changeAccountType,
    handleMute,
    handleUnmute,
    removeFromCall,
    endCall,
    lowerHand,
    leaveCall,
    modCount,
    raiseHand,
  ]);

  useEffect(() => {
    if (!participant?.audioTrack || !audioRef.current) return;
    // sanity check to make sure this is an audio track
    if (
      participant?.audioTrack?.track &&
      !participant?.audioTrack?.track?.kind === "audio"
    )
      return;
    // don't play the local audio track (echo!)
    if (participant?.local) return;
    // set the audio source for everyone else

    /**
      Note: Safari will block the autoplay of audio by default.

      Improvement: implement a timeout to check if audio stream is playing
      and prompt the user if not, e.g:
      
      let playTimeout;
      const handleCanPlay = () => {
        playTimeout = setTimeout(() => {
          showPlayAudioPrompt(true);
        }, 1500);
      };
      const handlePlay = () => {
        clearTimeout(playTimeout);
      };
      audioEl.current.addEventListener('canplay', handleCanPlay);
      audioEl.current.addEventListener('play', handlePlay);
     */
    audioRef.current.srcObject = new MediaStream([participant?.audioTrack]);
  }, [participant?.audioTrack, participant?.local]);

  useEffect(() => {
    // On iOS safari, when headphones are disconnected, all audio elements are paused.
    // This means that when a user disconnects their headphones, that user will not
    // be able to hear any other users until they mute/unmute their mics.
    // To fix that, we call `play` on each audio track on all devicechange events.

    if (!audioRef.current) {
      return false;
    }

    const startPlayingTrack = () => {
      audioRef.current?.play();
    };
    navigator.mediaDevices.addEventListener("devicechange", startPlayingTrack);

    return () => {
      navigator.mediaDevices.removeEventListener(
        "devicechange",
        startPlayingTrack
      );
    };
  }, [audioRef]);

  const showMoreMenu = useMemo(
    () => getAccountType(local?.user_name) === MOD || participant?.local,
    [getAccountType, local, participant]
  );

  return (
    <Container>
      <Avatar
        muted={!participant?.audio}
        isActive={activeSpeakerId === participant?.user_id}
      >
        <AvatarText>
          {participant?.owner ? ADMIN_BADGE : ""}
          {initials(participant?.user_name)}
        </AvatarText>
      </Avatar>
      <Name>{name}</Name>
      {getAccountType(participant?.user_name) !== LISTENER && (
        <AudioIcon>
          {participant?.audio ? <MicIcon /> : <MutedIcon />}
        </AudioIcon>
      )}
      {showMoreMenu && menuOptions.length > 0 && (
        <MenuButton onClick={() => setIsVisible(!isVisible)}>
          <MoreIcon />
        </MenuButton>
      )}
      {isVisible && (
        <MenuContainer ref={ref}>
          <Menu options={menuOptions} setIsVisible={setIsVisible} />
        </MenuContainer>
      )}
      {participant?.audioTrack && (
        <audio
          autoPlay
          playsInline
          id={`audio-${participant.user_id}`}
          ref={audioRef}
        />
      )}
    </Container>
  );
};

const Container = styled.div`
  display: flex;
  flex-direction: column;
  margin: 8px;
  align-items: flex-start;
  position: relative;
  max-width: 104px;
`;
const Avatar = styled.div`
  width: ${AVATAR_DIMENSION}px;
  height: ${AVATAR_DIMENSION}px;
  border-radius: 24px;
  background-color: ${(props) =>
    props.muted ? theme.colors.grey : theme.colors.turquoise};
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid
    ${(props) => (props.isActive ? theme.colors.teal : "transparent")};
`;
const AvatarText = styled.p`
  font-size: ${theme.fontSize.large};
  color: ${theme.colors.blueLight};
  font-weight: 600;
  line-height: 32px;
`;
const Name = styled.p`
  color: ${theme.colors.blueDark};
  margin: 8px 0;
  font-weight: 400;
  font-size: ${theme.fontSize.base};
  padding-left: 4px;
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 20px;
`;
const AudioIcon = styled.div`
  position: absolute;
  top: ${AVATAR_DIMENSION - 28}px;
  left: -4px;
`;
const MenuButton = styled.button`
  border: none;
  background-color: transparent;
  position: absolute;
  top: ${AVATAR_DIMENSION - 28}px;
  right: -4px;
  padding: 0;
  cursor: pointer;
`;
const MenuContainer = styled.div`
  position: absolute;
  bottom: 0;
  right: 0;
  z-index: 10;
`;

export default Participant; // React.memo(Participant, (p, n) => p.participant?.id === n.participant?.id);
