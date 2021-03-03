import React, {useEffect, useState} from 'react';
import {Text, StyleSheet} from 'react-native';
import {useCallState} from '../contexts/CallProvider';
import theme from './theme';

const Counter = () => {
  const {roomExp, leaveCall, view} = useCallState();
  const [counter, setCounter] = useState('');

  let interval;
  useEffect(() => {
    /**
     * Rooms exist for 10 minutes from creation.
     * We use the expiry timestamp to show participants
     * how long they have left in the room.
     */
    clearInterval(interval);
    interval = setInterval(() => {
      let secs = Math.round((roomExp - Date.now()) / 1000);
      const value = Math.floor(secs / 60) + ':' + ('0' + (secs % 60)).slice(-2);
      if (secs <= 0) {
        clearInterval(interval);
        console.log('Eep! Room has expired');
        leaveCall();
        return;
      }
      setCounter(value);
    }, 1000);

    return () => {
      clearInterval(interval);
    };
  }, [roomExp, view]);

  return (
    <Text style={styles.container}>
      Demo ends in <Text style={styles.count}>{counter}</Text>
    </Text>
  );
};

const styles = StyleSheet.create({
  container: {
    fontSize: theme.fontSize.base,
    color: theme.colors.greyDark,
  },
  counter: {
    width: 28,
    textAlign: 'right',
  },
});

export default Counter;
