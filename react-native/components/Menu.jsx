import React, {useRef} from 'react';
import {Image, StyleSheet, Pressable, Text} from 'react-native';
import Menu, {MenuItem} from 'react-native-material-menu';
import theme from './theme';

const ActionMenu = ({options}) => {
  const menuRef = useRef(null);

  const hideMenu = () => {
    if (!menuRef?.current) return;
    menuRef?.current.hide();
  };

  const showMenu = () => {
    if (!menuRef?.current) return;
    menuRef?.current.show();
  };

  return (
    <Menu
      ref={menuRef}
      animationDuration={0}
      button={
        <Pressable style={styles.showMore} onPress={showMenu}>
          <Image style={styles.moreIcon} source={require('./icons/more.png')} />
        </Pressable>
      }>
      {options.map((o, i) => (
        <MenuItem
          key={i}
          onPress={() => {
            o.action();
            hideMenu();
          }}>
          <Text
            style={[styles.text, o.warning && {color: theme.colors.redDark}]}>
            {o.text}
          </Text>
        </MenuItem>
      ))}
    </Menu>
  );
};

const styles = StyleSheet.create({
  text: {
    fontSize: theme.fontSize.large,
    color: theme.colors.blueDark,
    paddingVertical: 6,
    paddingHorizontal: 16,
    flexWrap: 'nowrap',
  },
});

export default ActionMenu;
