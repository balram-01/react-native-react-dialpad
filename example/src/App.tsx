import React, { useEffect } from 'react';
import { View, Text, Button } from 'react-native';
import { ReactDialpadView, requestRole, makeCall } from 'react-native-dialpad';
import Dimensions from 'react-native';

export default function App() {

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Button  title="Request Role" onPress={async () => {
        console.log("button pressed")
  try {
    const resp = await requestRole();
    console.log('Role response', resp);
  } catch (err) {
    console.log(err);
  }
}} />

    </View>
  );
}