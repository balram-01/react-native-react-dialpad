<div align="center">

<br />

<img src="https://capsule-render.vercel.app/api?type=waving&color=6C63FF&height=120&section=header&text=react-native-dialpad&fontSize=32&fontColor=ffffff&fontAlignY=38&desc=Powerful%20%C2%B7%20Native%20%C2%B7%20Seamless&descAlignY=60&descSize=14" width="100%" />

<br />

[![npm version](https://img.shields.io/npm/v/@balram_01/react-native-dialpad?color=6C63FF&style=for-the-badge&logo=npm&logoColor=white)](https://www.npmjs.com/package/@balram_01/react-native-dialpad)
[![TypeScript](https://img.shields.io/badge/TypeScript-100%25-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-lightgrey?style=for-the-badge)](#)

<br />

> **A powerful, native dialer and call management library for React Native.**

<br />

<h2 align="center">Screenshots</h2>

<div align="center">
  <img src="https://raw.githubusercontent.com/balram-01/react-native-react-dialpad/main/assets/images/image1.jpeg" width="220" alt="Incoming Call" />
  <img src="https://raw.githubusercontent.com/balram-01/react-native-react-dialpad/main/assets/images/image2.jpeg" width="220" alt="Dialing" />
  <img src="https://raw.githubusercontent.com/balram-01/react-native-react-dialpad/main/assets/images/image3.jpeg" width="220" alt="Call Actions" />
</div>

<br /><br />

</div>

---

## ✦ Why this library?

`react-native-dialpad` allows you to create fully-functional dialer applications, request default dialer system roles, fetch and manage contacts, and handle native Telecom/InCallService routing seamlessly.

| Feature | Description |
|---|---|
| **Native Call UI** | Hooks directly into Android's `InCallService` for native incoming/outgoing call screens. |
| **Telecom Integration** | Proper audio routing and microphone mute/unmute handling. |
| **Default Dialer Role** | Seamless methods to request the user to set your app as the default system dialer. |
| **Contact Management** | Fetch all contacts, create new contacts, and manage blocked numbers natively. |
| **Call Logs** | Easily fetch the device's call history directly into React Native. |
| **Call Settings** | Toggle vibrations, secure numbers, call forwarding, and quick reply management. |

---

## 📦 Installation

```bash
# npm
npm install @balram_01/react-native-dialpad

# yarn
yarn add @balram_01/react-native-dialpad
```

## ⚙️ Android Setup

To use this library as a default dialer, you **must** configure your Android Manifest properly.

### 1. Modify `AndroidManifest.xml`

Open `android/app/src/main/AndroidManifest.xml` and ensure you have the following permissions and intent filters:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

  <!-- Required Permissions for Dialer Functionality -->
  <uses-permission android:name="android.permission.CALL_PHONE" />
  <uses-permission android:name="android.permission.READ_CONTACTS" />
  <uses-permission android:name="android.permission.WRITE_CONTACTS" />
  <uses-permission android:name="android.permission.MANAGE_OWN_CALLS" />
  <uses-permission android:name="android.permission.READ_CALL_LOG" />
  <uses-permission android:name="android.permission.WRITE_CALL_LOG" />

  <application ...>
    <activity
      android:name=".MainActivity"
      android:exported="true">
      
      <!-- Standard Launcher Intent -->
      <intent-filter>
          <action android:name="android.intent.action.MAIN" />
          <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>

      <!-- REQUIRED: Intent filter to qualify as a system dialer -->
      <intent-filter>
          <action android:name="android.intent.action.DIAL" />
          <category android:name="android.intent.category.DEFAULT" />
      </intent-filter>
      
      <!-- Intent filter for handling tel: links -->
      <intent-filter>
          <action android:name="android.intent.action.DIAL" />
          <category android:name="android.intent.category.DEFAULT" />
          <data android:scheme="tel" />
      </intent-filter>

    </activity>
  </application>
</manifest>
```

---

## ⚡ Quick Start & Usage

### 1. Request Default Dialer Role

Before making calls through your custom UI, your app should be set as the default dialer.

```tsx
import { requestRole, checkIfDefaultDialer, openDialerSetting } from '@balram_01/react-native-dialpad';

const setupDialer = async () => {
  const isDefault = await checkIfDefaultDialer();
  
  if (!isDefault) {
    try {
      const result = await requestRole();
      if (result === 'Role granted') {
        console.log('App is now the default dialer!');
      }
    } catch (error) {
      console.error('User declined or request failed', error);
      // Fallback: Open settings for the user to manually set it
      await openDialerSetting();
    }
  }
};
```

### 2. Make a Call

Ensure you have requested runtime permissions (`CALL_PHONE`) before calling this method.

```tsx
import { makeCall } from '@balram_01/react-native-dialpad';
import { PermissionsAndroid } from 'react-native';

const handleMakeCall = async (phoneNumber: string) => {
  const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.CALL_PHONE);
  
  if (granted === PermissionsAndroid.RESULTS.GRANTED) {
    try {
      await makeCall(phoneNumber);
    } catch (e) {
      console.error('Call failed:', e);
    }
  }
};
```

### 3. Fetch Call Logs

Fetch recent call history natively from the device.

```tsx
import { getCallLogs } from '@balram_01/react-native-dialpad';

const fetchLogs = async () => {
  try {
    const logs = await getCallLogs();
    console.log(`Fetched ${logs.length} logs! Recent: ${logs[0]?.number}`);
  } catch (err) {
    console.error('Failed to fetch call logs', err);
  }
};
```

### 4. Fetch Contacts

Ensure you have requested the `READ_CONTACTS` permission at runtime.

```tsx
import { getAllContacts } from '@balram_01/react-native-dialpad';

const fetchContacts = async () => {
  try {
    const contacts = await getAllContacts();
    console.log(`Fetched ${contacts.length} contacts!`);
  } catch (err) {
    console.error('Failed to fetch contacts', err);
  }
};
```

---

## 📋 API Reference

| Method | Description | Return Type |
|---|---|---|
| `checkIfDefaultDialer()` | Checks if the app is currently the default dialer | `Promise<boolean>` |
| `getDefaultDialerPackage()` | Returns the package name of the default dialer | `Promise<string>` |
| `requestRole()` | Prompts user to set app as default dialer | `Promise<string>` |
| `openDialerSetting()` | Opens the default apps settings menu | `Promise<string>` |
| `makeCall(phone)` | Initiates a telecom call to the given number | `Promise<string>` |
| `getCallLogs()` | Fetches recent call history | `Promise<Array<CallLog>>` |
| `getAllContacts()` | Returns a list of the device's contacts | `Promise<Array<Contact>>` |
| `getContactById(id)` | Fetches detailed info for a specific contact | `Promise<ContactMap>` |
| `createNewContact(contact)`| Creates a new contact entry | `Promise<string>` |
| `updateContact(contact)` | Updates an existing contact | `Promise<string>` |
| `deleteContact(contact)` | Deletes a contact | `Promise<string>` |
| `isNumberBlocked(phone)` | Checks if a number is blocked | `Promise<boolean>` |
| `addBlockedNumber(phone)`| Blocks the specified phone number | `Promise<string>` |
| `removeBlockedNumber(phone)`| Unblocks the specified phone number | `Promise<string>` |
| `getBlockedNumbers()` | Returns a list of all blocked numbers | `Promise<Array<string>>` |
| `toggleVibration(value)` | Enables/disables vibration for incoming calls | `Promise<string>` |
| `forwardAllCalls(...)` | Configures Call Forwarding unconditionally | `Promise<string>` |

---

## 🤝 Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## 📄 License

MIT © [Balram](https://github.com/balram-01)

<br />

<div align="center">

If this saved you time, consider giving it a ⭐ on GitHub — it helps others find it.

<br />

<img src="https://capsule-render.vercel.app/api?type=waving&color=6C63FF&height=80&section=footer" width="100%" />

</div>
