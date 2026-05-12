import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Alert,
  PermissionsAndroid,
  Platform,
  Pressable,
  StatusBar,
  ScrollView,
} from 'react-native';
import { requestRole, makeCall, getAllContacts } from 'react-native-dialpad';
import { SafeAreaView } from 'react-native-safe-area-context';
const DIALPAD_BUTTONS = [
  { number: '1', letters: '' },
  { number: '2', letters: 'ABC' },
  { number: '3', letters: 'DEF' },
  { number: '4', letters: 'GHI' },
  { number: '5', letters: 'JKL' },
  { number: '6', letters: 'MNO' },
  { number: '7', letters: 'PQRS' },
  { number: '8', letters: 'TUV' },
  { number: '9', letters: 'WXYZ' },
  { number: '*', letters: '' },
  { number: '0', letters: '+' },
  { number: '#', letters: '' },
];

export default function App() {
  const [phoneNumber, setPhoneNumber] = useState('');

  const requestCallPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.CALL_PHONE
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return true;
  };

  const requestContactsPermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.READ_CONTACTS
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return true;
  };

  const handleKeyPress = (key: string) => {
    setPhoneNumber((prev) => {
      if (prev.length < 20) return prev + key;
      return prev;
    });
  };

  const handleDelete = () => {
    setPhoneNumber((prev) => prev.slice(0, -1));
  };

  const handleLongDelete = () => {
    setPhoneNumber('');
  };

  const handleCall = async () => {
    if (!phoneNumber) {
      Alert.alert('Notice', 'Please enter a phone number first.');
      return;
    }
    const hasPermission = await requestCallPermission();
    if (!hasPermission) {
      Alert.alert(
        'Permission Denied',
        'Call permission is required to make calls.'
      );
      return;
    }
    try {
      await makeCall(phoneNumber);
    } catch (err) {
      console.log('Call error:', err);
      Alert.alert('Error', 'Failed to make call.');
    }
  };

  const handleRequestRole = async () => {
    try {
      const resp = await requestRole();
      if (resp === 'Already Default Dialer') {
        Alert.alert('Status', 'App is already the default dialer.');
      } else if (resp === 'Accepted') {
        Alert.alert('Success', 'Successfully set as default dialer.');
      } else {
        Alert.alert('Role Requested', `Response: ${resp}`);
      }
    } catch (err) {
      console.log('Role error:', err);
      Alert.alert(
        'Notice',
        'User did not accept default dialer role or request failed.'
      );
    }
  };

  const handleGetContacts = async () => {
    const hasPermission = await requestContactsPermission();
    if (!hasPermission) {
      Alert.alert('Permission Denied', 'Contacts permission is required.');
      return;
    }
    try {
      const contacts = await getAllContacts();
      Alert.alert(
        'Contacts',
        `Successfully fetched ${contacts.length} contacts.`
      );
    } catch (err) {
      console.log('Contacts error:', err);
      Alert.alert('Error', 'Failed to fetch contacts.');
    }
  };

  const formatPhoneNumber = (number: string) => {
    // Simple formatting for display purposes (optional but nice for premium feel)
    if (number.length > 10) return number;
    if (number.length > 6) {
      return `${number.slice(0, 3)} ${number.slice(3, 6)} ${number.slice(6)}`;
    }
    if (number.length > 3) {
      return `${number.slice(0, 3)} ${number.slice(3)}`;
    }
    return number;
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#121212" />

      {/* Top Navigation / Actions */}
      <View style={styles.topBar}>
        <Pressable
          onPress={handleGetContacts}
          style={({ pressed }) => [styles.textBtn, pressed && styles.pressed]}
        >
          <Text style={styles.textBtnText}>Contacts</Text>
        </Pressable>
        <Pressable
          onPress={handleRequestRole}
          style={({ pressed }) => [styles.textBtn, pressed && styles.pressed]}
        >
          <Text style={styles.textBtnText}>Set Default</Text>
        </Pressable>
      </View>

      {/* Number Display Area */}
      <View style={styles.displayContainer}>
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.displayScrollContent}
        >
          <Text
            style={[
              styles.numberDisplay,
              phoneNumber.length > 12 && styles.numberDisplaySmall,
            ]}
            numberOfLines={1}
          >
            {formatPhoneNumber(phoneNumber)}
          </Text>
        </ScrollView>
      </View>

      {/* Keypad */}
      <View style={styles.keypadContainer}>
        <View style={styles.keypadGrid}>
          {DIALPAD_BUTTONS.map((btn) => (
            <View key={btn.number} style={styles.buttonWrapper}>
              <Pressable
                onPress={() => handleKeyPress(btn.number)}
                style={({ pressed }) => [
                  styles.dialButton,
                  pressed && styles.dialButtonPressed,
                ]}
              >
                <Text style={styles.dialButtonNumber}>{btn.number}</Text>
                <Text style={styles.dialButtonLetters}>{btn.letters}</Text>
              </Pressable>
            </View>
          ))}
        </View>

        {/* Action Row */}
        <View style={styles.actionRow}>
          <View style={styles.actionButtonWrapper} />
          <View style={styles.actionButtonWrapper}>
            <Pressable
              onPress={handleCall}
              style={({ pressed }) => [
                styles.callButton,
                pressed && styles.callButtonPressed,
              ]}
            >
              <Text style={styles.callButtonIcon}>📞</Text>
            </Pressable>
          </View>
          <View style={styles.actionButtonWrapper}>
            {phoneNumber.length > 0 && (
              <Pressable
                onPress={handleDelete}
                onLongPress={handleLongDelete}
                delayLongPress={400}
                style={({ pressed }) => [
                  styles.deleteButton,
                  pressed && styles.deleteButtonPressed,
                ]}
              >
                <Text style={styles.deleteButtonIcon}>⌫</Text>
              </Pressable>
            )}
          </View>
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#121212',
  },
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 24,
    paddingTop: 16,
  },
  textBtn: {
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 8,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
  },
  textBtnText: {
    color: '#0A84FF',
    fontSize: 16,
    fontWeight: '500',
  },
  pressed: {
    opacity: 0.7,
  },
  displayContainer: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
    paddingBottom: 24,
    paddingHorizontal: 20,
  },
  displayScrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  numberDisplay: {
    fontSize: 48,
    fontWeight: '400',
    color: '#FFFFFF',
    letterSpacing: 2,
  },
  numberDisplaySmall: {
    fontSize: 36,
  },
  keypadContainer: {
    paddingHorizontal: 30,
    paddingBottom: Platform.OS === 'ios' ? 40 : 30,
  },
  keypadGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  buttonWrapper: {
    width: '33.33%',
    alignItems: 'center',
    marginBottom: 24,
  },
  dialButton: {
    width: 76,
    height: 76,
    borderRadius: 38,
    backgroundColor: '#2C2C2E',
    justifyContent: 'center',
    alignItems: 'center',
  },
  dialButtonPressed: {
    backgroundColor: '#3A3A3C',
    transform: [{ scale: 0.95 }],
  },
  dialButtonNumber: {
    fontSize: 32,
    color: '#FFFFFF',
    fontWeight: '400',
  },
  dialButtonLetters: {
    fontSize: 11,
    color: '#8E8E93',
    fontWeight: '600',
    letterSpacing: 1,
    marginTop: -2,
    height: 14,
  },
  actionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 10,
  },
  actionButtonWrapper: {
    width: '33.33%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  callButton: {
    width: 76,
    height: 76,
    borderRadius: 38,
    backgroundColor: '#30D158',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#30D158',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  callButtonPressed: {
    backgroundColor: '#28B84D',
    transform: [{ scale: 0.95 }],
  },
  callButtonIcon: {
    fontSize: 32,
  },
  deleteButton: {
    width: 60,
    height: 60,
    justifyContent: 'center',
    alignItems: 'center',
  },
  deleteButtonPressed: {
    opacity: 0.6,
  },
  deleteButtonIcon: {
    fontSize: 28,
    color: '#8E8E93',
  },
});
