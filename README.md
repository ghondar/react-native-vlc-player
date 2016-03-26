### React-Native-VLC-Player

> VLC Player for react-native

*Only Android support now.*

#### Integrate

##### Android

##### Install via npm
`npm i react-native-vlc-player --save`

##### Configure vlc library

 Download file [here](https://github.com/ghondar/react-native-vlc-player/raw/master/android/libvlc/libvlc.aar)

 And put libvlc.aar on `node_modules/react-native-vlc-player/android/libvlc`

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
##### Register module in `MainActivity.java`
```Java
import com.ghondar.vlcplayer.*;  // <--- import

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReactRootView = new ReactRootView(this);

        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new VLCPlayerPackage())  // <------- here
                .addPackage(new MainReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();

        mReactRootView.startReactApplication(mReactInstanceManager, "doubanbook", null);

        setContentView(mReactRootView);
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
          onPress={() => { play('/storage/emulated/0/example.avi') }}>
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
