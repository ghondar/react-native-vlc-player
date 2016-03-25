### React-Native-VLC-Player

> VLC Player for react-native

*Only Android support now.*

#### Integrate

##### Android

* Install via npm
`npm i react-native-vlc-player --save`

* Add dependency to `android/settings.gradle`
```
...
include ':react-native-vlc-player'
project(':react-native-vlc-player').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-vlc-player/android/vlc')
```

* Add `android/app/build.gradle`
```
...
dependencies {
    ...
    compile project(':react-native-vlc-player')
}
```
* Register module in `MainActivity.java`
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
import React, { Component, View, Text, TouchableHighlight } from 'react-native'

import { play } from './lib/VLCPlayer'

class Example extends Component {
  constructor(props, context) {
    super(props, context)
  }

  onStart() {
    play('/storage/emulated/0/video.mp4')
  }

  render() {

    return (
      <View style={styles.container}>

        <TouchableHighlight
          onPress={this.onStart}>
            <Text >Play Video!</Text>
        </TouchableHighlight>

      </View>
    )
  }
}

export default Example

```

#### LICENSE
MIT
