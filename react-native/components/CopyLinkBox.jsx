import React, {useState} from 'react';
import {StyleSheet, Pressable, View, Text, Platform} from 'react-native';
import Clipboard from '@react-native-clipboard/clipboard';
import theme from './theme';

const CopyLinkBox = ({room}) => {
  const [linkCopied, setLinkCopied] = useState(false);

  const copyToClipboard = () => {
    Clipboard.setString(room?.name);
    setLinkCopied(true);
    setTimeout(() => {
      setLinkCopied(false);
    }, 5000);
  };

  return (
    <View style={styles.container}>
      <View style={styles.inviteContainer}>
        <Text style={styles.header}>Invite others</Text>
        <Text style={styles.subheader}>
          Copy and share join code with others to invite them. Code:{' '}
          <Text style={styles.bold}>{room?.name}</Text>
        </Text>
        <Pressable
          onPress={copyToClipboard}
          style={({pressed}) => [
            {
              backgroundColor: pressed
                ? theme.colors.cyan
                : theme.colors.turquoise,
            },
            styles.buttonContainer,
          ]}>
          <Text style={styles.buttonText}>
            {linkCopied ? 'Copied!' : `Copy join code`}
          </Text>
        </Pressable>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    marginVertical: 24,
    maxWidth: 330,
    marginLeft: 'auto',
    marginRight: 'auto',
    zIndex: 1,
    elevation: Platform.OS === 'android' ? 1 : 0,
  },
  inviteContainer: {
    borderWidth: 1,
    borderColor: theme.colors.grey,
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
  },
  header: {
    color: theme.colors.blueDark,
    marginBottom: 8,
    fontSize: theme.fontSize.large,
    fontWeight: '600',
  },
  subheader: {
    textAlign: 'center',
    fontSize: theme.fontSize.med,
    color: theme.colors.blueDark,
  },
  bold: {
    fontWeight: '600',
  },
  buttonContainer: {
    overflow: 'hidden',
    marginBottom: 8,
    borderColor: theme.colors.cyanLight,
    borderWidth: 1,
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 24,
    marginTop: 16,
  },
  buttonText: {
    fontSize: theme.fontSize.med,
    fontWeight: '600',
    textAlign: 'center',
  },
});

export default CopyLinkBox;
