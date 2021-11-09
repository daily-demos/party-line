import React, {useMemo} from 'react';
import {
  Pressable,
  View,
  Text,
  StyleSheet,
  Platform,
  LogBox,
} from 'react-native';
import {
  INCALL,
  useCallState,
  SPEAKER,
  LISTENER,
} from '../contexts/CallProvider';
import Participant from './Participant';
import CopyLinkBox from './CopyLinkBox';
import Counter from './Counter';
import theme from './theme';

const InCall = ({handleLinkPress}) => {
  const {participants, room, view, getAccountType} = useCallState();
  console.log(participants);

  /**
   * Turn off logs for demo. Comment out line below for debugging.
   */
  LogBox.ignoreAllLogs(true);

  const mods = useMemo(() => participants?.filter((p) => p?.owner), [
    participants,
    getAccountType,
  ]);

  const speakers = useMemo(
    (p) =>
      participants?.filter((p) => getAccountType(p?.user_name) === SPEAKER),
    [participants, getAccountType],
  );
  const local = useMemo((p) => participants?.filter((p) => p?.local)[0], [
    participants,
  ]);

  const listeners = useMemo(() => {
    const l = participants
      ?.filter((p) => getAccountType(p?.user_name) === LISTENER)
      .sort((a, _) => {
        // Move raised hands to front of list
        if (a?.user_name.includes('✋')) return -1;
        return 0;
      });
    return (
      <View style={styles.listenersContainer}>
        {l?.map((p, i) => (
          <Participant
            participant={p}
            key={p.id}
            local={local}
            zIndex={l.length - i}
          />
        ))}
      </View>
    );
  }, [participants, getAccountType]);

  const canSpeak = useMemo(() => {
    const s = [...mods, ...speakers];
    return (
      <View style={styles.speakersContainer}>
        {s?.map((p, i) => (
          <Participant participant={p} key={p.id} local={local} />
        ))}
      </View>
    );
  }, [mods, speakers]);

  return (
    <View
      style={[
        styles.container,
        {visibility: view === INCALL ? 'visible' : 'hidden'},
      ]}>
      <View style={styles.content}>
        <View style={styles.header}>
          <Text style={styles.headerText}>Speakers</Text>
          <Counter />
        </View>
        {canSpeak}
        <Text style={styles.headerText}>Listeners</Text>
        {listeners}
      </View>
      <CopyLinkBox
        room={room}
        style={{zIndex: 30, elevation: Platform.OS === 'android' ? 30 : 0}}
      />
      <Pressable onPress={handleLinkPress}>
        <Text style={styles.link}>Learn more about this demo</Text>
      </Pressable>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
    flex: 1,
  },
  content: {
    paddingHorizontal: 24,
    zIndex: 30,
  },
  speakersContainer: {
    flexDirection: 'row',
    borderBottomColor: theme.colors.grey,
    borderBottomWidth: 1,
    marginBottom: 24,
    zIndex: 20,
    flexWrap: 'wrap',
  },
  listenersContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 24,
    zIndex: 10,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  headerText: {
    color: theme.colors.greyDark,
    fontSize: theme.fontSize.xlarge,
  },
  link: {
    fontWeight: '400',
    fontSize: theme.fontSize.med,
    color: theme.colors.greyDark,
    textAlign: 'center',
    textDecorationLine: 'underline',
  },
});

export default InCall;
