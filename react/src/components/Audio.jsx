import React, {useRef, useCallback, useEffect} from "react";

export const AudioItem = ({participant}) => {
    const audioRef = useRef(null);

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
