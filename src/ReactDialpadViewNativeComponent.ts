import { codegenNativeComponent } from 'react-native';
import type { ViewProps } from 'react-native';

interface NativeProps extends ViewProps {
  color?: string;
}

export default codegenNativeComponent<NativeProps>('ReactDialpadView');
