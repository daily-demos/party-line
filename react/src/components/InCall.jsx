import { useMemo, useCallback } from "react";
import styled from "styled-components";
import { INCALL, useCallState } from "../CallProvider";
import { SPEAKER, LISTENER, MOD } from "../App";
import CopyLinkBox from "./CopyLinkBox";
import Participant from "./Participant";
import Audio from "./Audio";
import Counter from "./Counter";
import MicIcon from "./MicIcon";
import MutedIcon from "./MutedIcon";
import theme from "../theme";

const InCall = () => {
  const {
    participants,
    room,
    view,
    getAccountType,
    leaveCall,
    handleMute,
    handleUnmute,
    raiseHand,
    lowerHand,
    endCall,
  } = useCallState();
  console.log(participants);

  const local = useMemo(
    (p) => participants?.filter((p) => p?.local)[0],
    [participants]
  );

  const mods = useMemo(
    () =>
      participants?.filter(
        (p) => p?.owner && getAccountType(p?.user_name) === MOD
      ),
    [participants, getAccountType]
  );
  const speakers = useMemo(
    (p) =>
      participants?.filter((p) => getAccountType(p?.user_name) === SPEAKER),
    [participants, getAccountType]
  );
  const listeners = useMemo(() => {
    const l = participants
      ?.filter((p) => getAccountType(p?.user_name) === LISTENER)
      .sort((a, _) => {
        // Move raised hands to front of list
        if (a?.user_name.includes("✋")) return -1;
        return 0;
      });
    return (
      <ListeningContainer>
        {l?.map((p, i) => (
          <Participant
            participant={p}
            key={`listening-${p.user_id}`}
            local={local}
            modCount={mods?.length}
          />
        ))}
      </ListeningContainer>
    );
  }, [participants, getAccountType, local, mods]);

  const canSpeak = useMemo(() => {
    const s = [...mods, ...speakers];
    return (
      <CanSpeakContainer>
        {s?.map((p, i) => (
          <Participant
            participant={p}
            key={`speaking-${p.user_id}`}
            local={local}
            modCount={mods?.length}
          />
        ))}
      </CanSpeakContainer>
    );
  }, [mods, speakers, local]);

  const handleAudioChange = useCallback(
    () => (local?.audio ? handleMute(local) : handleUnmute(local)),
    [handleMute, handleUnmute, local]
  );
  const handleHandRaising = useCallback(
    () =>
      local?.user_name.includes("✋") ? lowerHand(local) : raiseHand(local),
    [lowerHand, raiseHand, local]
  );

  return (
    <>
    <Container hidden={view !== INCALL}>
      <CallHeader>
        <Header>Speakers</Header>
        <Counter />
      </CallHeader>
      {canSpeak}
      <Header>Listeners</Header>
      {listeners}
      <CopyLinkBox room={room} />
      <Tray>
        <TrayContent>
          {[MOD, SPEAKER].includes(getAccountType(local?.user_name)) ? (
            <AudioButton onClick={handleAudioChange}>
              {local?.audio ? (
                <MicIcon type="simple" />
              ) : (
                <MutedIcon type="simple" />
              )}
              <ButtonText>{local?.audio ? "Mute" : "Unmute"}</ButtonText>
            </AudioButton>
          ) : (
            <HandButton onClick={handleHandRaising}>
              <ButtonText>
                {local?.user_name.includes("✋")
                  ? "Lower hand"
                  : "Raise hand ✋"}
              </ButtonText>
            </HandButton>
          )}
          {mods?.length < 2 && getAccountType(local?.user_name) === MOD ? (
            <LeaveButton onClick={endCall}>End call</LeaveButton>
          ) : (
            <LeaveButton onClick={leaveCall}>Leave call</LeaveButton>
          )}
        </TrayContent>
      </Tray>
    </Container>
    <Audio participants={participants}/>
    </>
  );
};

const Container = styled.div`
  margin: 48px 0 0;
  visibility: ${(props) => (props.hidden ? "hidden" : "visible")};
  height: ${(props) => (props.hidden ? "0" : "100%")};
`;
const CanSpeakContainer = styled.div`
  border-bottom: ${theme.colors.grey} 1px solid;
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;
`;
const ListeningContainer = styled.div`
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
`;
const Header = styled.h2`
  font-size: ${theme.fontSize.large};
  color: ${theme.colors.greyDark};
`;
const CallHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
`;
const Tray = styled.div`
  display: flex;
  justify-content: center;
  position: absolute;
  bottom: 0;
  left: 0;
  height: 52px;
  width: 100vw;
  box-sizing: border-box;
  background-color: ${theme.colors.greyLight};
  padding: 12px;
`;
const TrayContent = styled.div`
  max-width: 700px;
  display: flex;
  justify-content: space-between;
  width: 100%;
`;
const Button = styled.button`
  font-size: ${theme.fontSize.large};
  font-weight: 600;
  border: none;
  background-color: transparent;
  cursor: pointer;
  border-radius: 8px;

  &:hover {
    background-color: ${theme.colors.greyLightest};
  }
`;
const LeaveButton = styled(Button)`
  margin-left: auto;
`;
const HandButton = styled(Button)`
  margin-right: auto;
`;
const AudioButton = styled(Button)`
  margin-right: auto;
  display: flex;
  align-items: center;
`;
const ButtonText = styled.span`
  margin-left: 4px;
`;

export default InCall;
