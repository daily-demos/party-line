import React from 'react';
import {StyleSheet, ScrollView, View, StatusBar, Linking} from 'react-native';
import InCall from './InCall';
import PreJoinRoom from './PreJoinRoom';
import Tray from './Tray';
import {
  PREJOIN,
  INCALL,
  CallProvider,
  useCallState,
} from '../contexts/CallProvider';
import theme from './theme';
import Header from './Header';

const AppContent = () => {
  const {view} = useCallState();

  const handleLinkPress = async () => {
    const url = 'https://docs.daily.co/reference';
    const supported = await Linking.canOpenURL(url);

    if (supported) {
      await Linking.openURL(url);
    }
  };

  return (
    <View style={styles.appContainer}>
      <ScrollView>
        <StatusBar barStyle="dark-content" />
        <View style={styles.wrapper}>
          <Header />
          {view === PREJOIN && (
            <PreJoinRoom handleLinkPress={handleLinkPress} />
          )}
          {view === INCALL && <InCall handleLinkPress={handleLinkPress} />}
        </View>
      </ScrollView>
      {view === INCALL && <Tray />}
    </View>
  );
};

function App() {
  return (
    <CallProvider>
      <AppContent />
    </CallProvider>
  );
}

const styles = StyleSheet.create({
  appContainer: {
    backgroundColor: theme.colors.greyLightest,
    flex: 1,
  },
  wrapper: {
    paddingTop: 48,
    paddingBottom: 80,
    flex: 1,
    marginVertical: 0,
    marginHorizontal: 'auto',
  },
});

export default App;
