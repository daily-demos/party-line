import React from 'react';
import {
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
  Image,
  Linking,
} from 'react-native';
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

const AppContent = () => {
  const {view} = useCallState();

  const handleLinkPress = async () => {
    const url = 'https://docs.daily.co/docs/reference-docs';
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
          <View style={styles.header}>
            <View style={styles.headerTop}>
              <Text style={styles.title}>Party line</Text>
              <Image
                source={require('./icons/logo.png')}
                style={styles.logo}
                alt="logo"
              />
            </View>
            <Text style={styles.smallText}>An audio API demo from Daily</Text>
          </View>
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
  header: {
    paddingHorizontal: 24,
  },
  headerTop: {
    alignItems: 'center',
    justifyContent: 'space-between',
    flexDirection: 'row',
  },
  title: {
    fontSize: theme.fontSize.xxlarge,
    marginHorizontal: 0,
    color: theme.colors.blueDark,
    fontWeight: '600',
  },
  logo: {
    height: 24,
  },
  smallText: {
    fontSize: theme.fontSize.large,
    color: theme.colors.greyDark,
    fontWeight: '400',
    marginTop: 8,
    marginBottom: 24,
    marginHorizontal: 0,
  },
});

export default App;
