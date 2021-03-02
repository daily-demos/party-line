import React, {useMemo, useState} from 'react';
import {DailyMediaView} from '@daily-co/react-native-daily-js';
import {View, Text, StyleSheet, Image, Platform} from 'react-native';
import theme from './theme';
import {useCallState, LISTENER, MOD, SPEAKER} from '../contexts/CallProvider';
import Menu from './Menu';

const AVATAR_DIMENSION = 104;
const ADMIN_BADGE = '⭐ ';

const initials = (name) =>
  name
    ? name
        .split(' ')
        .map((n) => n.charAt(0))
        .join('')
    : '';

const Participant = ({participant, local, modCount, zIndex}) => {
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
  const name = displayName(participant?.user_name);

  const menuOptions = useMemo(() => {
    const mutedText = participant?.audio ? 'Mute' : 'Unmute';

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
        text: 'Mute',
        action: () => handleMute(participant),
      });
    }

    switch (getAccountType(participant?.user_name)) {
      case SPEAKER:
        if (!participant?.local) {
          const o = [
            {
              text: 'Make moderator',
              action: () => changeAccountType(participant, MOD),
            },
            {
              text: 'Make listener',
              action: () => changeAccountType(participant, LISTENER),
            },
            {
              text: 'Remove from call',
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
            text: participant?.user_name.includes('✋')
              ? 'Lower hand'
              : 'Raise hand ✋',
            action: participant?.user_name.includes('✋')
              ? () => lowerHand(participant)
              : () => raiseHand(participant),
          });
        } else {
          const o = [
            {
              text: 'Make moderator',
              action: () => changeAccountType(participant, MOD),
            },
            {
              text: 'Make speaker',
              action: () => changeAccountType(participant, SPEAKER),
            },
            {
              text: 'Remove from call',
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
        text: lastMod ? 'End call' : 'Leave call',
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

  const showMoreMenu = useMemo(() => {
    return getAccountType(local?.user_name) === MOD || participant?.local;
  }, [getAccountType, local, participant]);

  const audioTrack = useMemo(
    () =>
      participant?.tracks?.audio?.state === 'playable'
        ? participant?.tracks?.audio?.track
        : null,
    [participant?.tracks?.audio?.state],
  );

  return (
    <View
      style={[
        styles.container,
        {zIndex, elevation: Platform.OS === 'android' ? zIndex : 0},
      ]}>
      <View
        style={[
          styles.avatar,
          activeSpeakerId === participant?.user_id && styles.isActive,
          !participant?.audio && styles.isMuted,
        ]}>
        <Text style={styles.initials} numberOfLines={1}>
          {participant?.owner ? ADMIN_BADGE : ''}
          {initials(participant?.user_name)}
        </Text>
      </View>
      {getAccountType(participant?.user_name) !== LISTENER && (
        <View style={styles.audioIcon}>
          {participant?.audio ? (
            <Image source={require('./icons/mic.png')} />
          ) : (
            <Image source={require('./icons/muted.png')} />
          )}
        </View>
      )}
      <Text style={styles.name} numberOfLines={1}>
        {name}
      </Text>
      {showMoreMenu && menuOptions.length > 0 && (
        <View style={styles.menu} elevation={3}>
          <Menu options={menuOptions} />
        </View>
      )}
      {audioTrack && (
        <DailyMediaView
          id={`audio-${participant.user_id}`}
          videoTrack={null}
          audioTrack={audioTrack}
        />
      )}
    </View>
  );
};
const styles = StyleSheet.create({
  container: {
    margin: 8,
    position: 'relative',
    maxWidth: 104,
  },
  avatar: {
    width: AVATAR_DIMENSION,
    height: AVATAR_DIMENSION,
    borderRadius: 24,
    backgroundColor: theme.colors.turquoise,
    borderWidth: 2,
    borderColor: 'transparent',
    justifyContent: 'center',
    alignItems: 'center',
  },
  initials: {
    fontSize: theme.fontSize.xlarge,
    color: theme.colors.blueLight,
    fontWeight: '600',
    lineHeight: 32,
  },
  isActive: {
    borderColor: theme.colors.teal,
  },
  isMuted: {
    backgroundColor: theme.colors.grey,
  },
  name: {
    color: theme.colors.blueDark,
    fontWeight: '400',
    fontSize: theme.fontSize.large,
    overflow: 'hidden',
    marginTop: 8,
  },
  audioIcon: {
    position: 'absolute',
    top: AVATAR_DIMENSION - 28,
    left: -4,
  },
  showMore: {
    backgroundColor: theme.colors.white,
    padding: 4,
    borderRadius: 24,
    position: 'absolute',
    top: -50,
    right: -6,
  },
  menu: {
    position: 'absolute',
    top: AVATAR_DIMENSION - 28,
    right: -4,
    zIndex: 15,
    backgroundColor: theme.colors.white,
    padding: 4,
    borderRadius: 16,
    shadowColor: theme.colors.blue,
    shadowOpacity: 0.2,
    shadowRadius: 2,
    shadowOffset: {
      height: 1,
      width: 1,
    },
  },
});

export default Participant;
