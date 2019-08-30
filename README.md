# react-native-vlc-player

## Getting started

`$ npm install react-native-vlc-player --save`

## Dependencies

`$ npm install react-native-vector-icons --save`

### Manual installation

Copy Ionicons.ttf from `node_modules/react-native-vector-icons/Fonts` to `android/app/src/main/assets/fonts`


#### Android

1. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
```Diff
...
allprojects {
	...
	dependencies {
			...
+      maven {
+        url("https://jitpack.io")
+      }
	}
}
```


## Usage
```javascript
import React, {Component} from 'react';
import {
  StyleSheet,
  View
} from 'react-native';
// Import library
import VlcPlayer from 'react-native-vlc-player';

export default class App extends Component {
  vlcplayer = React.createRef();

  componentDidMount() {
    console.log(this.vlcplayer)
  }

  render() {
    return (
      <View
        style={[
          styles.container
        ]}>
        <VlcPlayer
          ref={this.vlcplayer}
          style={{
            width: 300,
            height: 200,
          }}
          paused={false}
          autoplay={true}
          source={{
            uri: 'file:///storage/emulated/0/Download/example.mp4',
            autoplay: true,
            initOptions: ['--codec=avcodec'],
          }}  />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'grey',
  },
});
```

## Props
```javascript
<VLCPlayer
	ref='vlcplayer'
	paused={this.state.paused}
	style={styles.vlcplayer}
	source={{uri: this.props.uri, initOptions: ['--codec=avcodec']}}
	onVLCProgress={this.onProgress.bind(this)}
	onVLCEnded={this.onEnded.bind(this)}
	onVLCStopped={this.onEnded.bind(this)}
	onVLCPlaying={this.onPlaying.bind(this)}
	onVLCBuffering={this.onBuffering.bind(this)}
	onVLCPaused={this.onPaused.bind(this)}
/>
```

## Static Methods

`seek(seconds)`

```
this.refs['vlcplayer'].seek(0.333);
```

`snapshot(path)`

```
this.refs['vlcplayer'].snapshot(path);
```
