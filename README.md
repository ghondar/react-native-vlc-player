### React-Native-VLC-Player

> VLC Player for react-native

*Only Android support now.*

![](https://media.giphy.com/media/l4hLFPgXI7ipAAMGk/giphy.gif)

#### Integrate

##### Android

##### Install via npm
`npm i https://github.com/Rob2k9/react-native-vlc-player`

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
    implementation project(':react-native-vlc-player')
}
```

##### Split APK for each CPU type `android/app/build.gradle`
```Java
    splits {
        abi {
            enable true
            reset()
            include "armeabi-v7a", "x86", "arm64-v8a", "x86_64"
            universalApk true
        }
    }
// This is a must due to the large file size for all VLCLibs Included
```

##### Register module in `MainApplication.java`
```Java
import com.ghondar.vlcplayer.VLCPlayerPackage;   // <--- import
// you do not need to do anything else int his file thanks to react auto linking 
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
