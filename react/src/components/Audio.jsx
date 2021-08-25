import React, {useRef, useEffect} from "react";

export const AudioItem = ({participant}) => {
    const audioRef = useRef(null);

    useEffect(() => {
    if (!participant?.audioTrack || !audioRef.current || participant?.local) return;
    // sanity check to make sure this is an audio track

    if (
      participant?.audioTrack?.track &&
      !participant?.audioTrack?.track?.kind === "audio"
    )
      return;
    audioRef.current.srcObject = new MediaStream([participant?.tracks.audio.persistentTrack]);
  }, [participant]);


  useEffect(() => {
    // On iOS safari, when headphones are disconnected, all audio elements are paused.
    // This means that when a user disconnects their headphones, that user will not
    // be able to hear any other users until they mute/unmute their mics.
    // To fix that, we call `play` on each audio track on all devicechange events.
    if (audioRef.currenet) {
      return false;
    }
    const startPlayingTrack = () => {
      audioRef.current?.play();
    };

    navigator.mediaDevices.addEventListener(
      'devicechange',
      startPlayingTrack
    );

    return () =>
      navigator.mediaDevices.removeEventListener(
        'devicechange',
        startPlayingTrack
      );
  }, [audioRef]);

    return (<>
            <audio
                autoPlay
                playsInline
                id={`audio-${participant.user_id}`}
                ref={audioRef}
            />
          </>)
};


export const Audio = ({participants}) => { 
    return <>
      {participants.map((p) => <AudioItem participant={p} key={`p-${p.user_id}`}/>)}
    </>
};

export default Audio;
