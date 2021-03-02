import React, {useCallback, useMemo} from 'react';
import {Dimensions} from 'react-native';
import {Platform} from 'react-native';
import {Pressable, View, Text, Image, StyleSheet} from 'react-native';
import {SPEAKER, MOD, useCallState} from '../contexts/CallProvider';
import theme from './theme';

const Tray = () => {
  const {
    getAccountType,
    leaveCall,
    handleMute,
    handleUnmute,
    participants,
    endCall,
    lowerHand,
    raiseHand,
  } = useCallState();

  const local = useMemo(() => participants?.filter((p) => p?.local)[0], [
    participants,
  ]);

  const handleAudioChange = useCallback(() => {
    return local?.audio ? handleMute(local) : handleUnmute(local);
  }, [handleMute, handleUnmute, local]);
  const mods = useMemo(() => participants?.filter((p) => p?.owner), [
    participants,
    getAccountType,
  ]);

  const pressedStyle = ({pressed}) => [
    {
      borderColor: pressed ? theme.colors.greyLightest : theme.colors.grey,
      borderWidth: 2,
    },
    styles.button,
  ];

  const handleHandRaising = useCallback(
    () =>
      local?.user_name.includes('✋') ? lowerHand(local) : raiseHand(local),
    [lowerHand, raiseHand, local],
  );

  return (
    <View style={styles.tray}>
      {[MOD, SPEAKER].includes(getAccountType(local?.user_name)) ? (
        <Pressable onPress={handleAudioChange} style={pressedStyle}>
          <View style={styles.textContainer}>
            {local?.audio ? (
              <Image source={require('./icons/simple_mic.png')} />
            ) : (
              <Image source={require('./icons/simple_muted.png')} />
            )}
            <Text style={styles.buttonText}>
              {local?.audio ? 'Mute' : 'Unmute'}
            </Text>
          </View>
        </Pressable>
      ) : (
        <Pressable onPress={handleHandRaising}>
          <View style={styles.textContainer}>
            <Text style={[styles.buttonText, {paddingTop: 4}]}>
              {local?.user_name.includes('✋') ? 'Lower hand' : 'Raise hand ✋'}
            </Text>
          </View>
        </Pressable>
      )}
      {mods?.length < 2 && getAccountType(local?.user_name) === MOD ? (
        <Pressable onPress={endCall} style={pressedStyle}>
          <Text style={styles.leaveText}>End call</Text>
        </Pressable>
      ) : (
        <Pressable onPress={leaveCall} style={pressedStyle}>
          <Text style={styles.leaveText}>Leave call</Text>
        </Pressable>
      )}
    </View>
  );
};
const height = Dimensions.get('window').height;
const trayHeight = Platform.OS === 'ios' ? 60 : 100;
const styles = StyleSheet.create({
  tray: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    position: 'absolute',
    height: trayHeight,
    top: height - trayHeight,
    width: '100%',
    backgroundColor: theme.colors.grey,
    paddingVertical: 12,
    paddingHorizontal: 24,
  },
  button: {
    paddingVertical: 4,
    borderRadius: 8,
  },
  textContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  buttonText: {
    marginLeft: 4,
    color: theme.colors.blueDark,
    fontSize: theme.fontSize.xlarge,
    fontWeight: '600',
  },
  leaveText: {
    color: theme.colors.blueDark,
    fontSize: theme.fontSize.xlarge,
    fontWeight: '600',
  },
});

export default Tray;
