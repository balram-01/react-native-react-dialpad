import { View, Button } from 'react-native';
import { requestRole } from 'react-native-dialpad';

export default function App() {
  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Button
        title="Request Role"
        onPress={async () => {
          console.log('button pressed');
          try {
            const resp = await requestRole();
            console.log('Role response', resp);
          } catch (err) {
            console.log(err);
          }
        }}
      />
    </View>
  );
}
