### React-Native-VLC-Player

> VLC Player for react-native

*Only Android support now.*

![](https://media.giphy.com/media/l4hLFPgXI7ipAAMGk/giphy.gif)

#### Integrate

##### Android

##### Install via npm
`npm i react-native-vlc-player --save`

##### Add dependency to `android/settings.gradle`
```
...
include ':libvlc'
project(':libvlc').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-vlc-player/android/libvlc')

include ':react-native-vlc-player'
project(':react-native-vlc-player').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-vlc-player/android/vlc')
```

##### Add `android/app/build.gradle`
```
...
dependencies {
    ...
    compile project(':react-native-vlc-player')
}
```
##### Register module in `MainApplication.java`
```Java
import com.ghondar.vlcplayer.*;  // <--- import

@Override
 protected List<ReactPackage> getPackages() {
   return Arrays.<ReactPackage>asList(
      new VLCPlayerPackage(),  // <------- here
      new MainReactPackage()
   );
 }
```

#### Usage

```Javascript
import React, { AppRegistry, StyleSheet, Component, View, Text, TouchableHighlight } from 'react-native'

import { play } from 'react-native-vlc-player'

class Example extends Component {
  constructor(props, context) {
    super(props, context)
  }

  render() {

    return (
      <View style={styles.container}>

        <TouchableHighlight
          onPress={() => { play('file:///storage/emulated/0/example.avi') }}>
            <Text >Play Video!</Text>
        </TouchableHighlight>

      </View>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  }
});

AppRegistry.registerComponent('example', () => Example);
```

#### LICENSE
MIT
