import React from 'react';
import {StyleSheet, View, Text, Image} from 'react-native';
import theme from './theme';

function Header() {
  return (
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
  );
}

const styles = StyleSheet.create({
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

export default Header;
