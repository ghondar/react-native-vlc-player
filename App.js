/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {NativeModules} from 'react-native';
import { play } from 'react-native-vlc-player'

var VideoView = require('./VlcPlayerView');

import {
  Platform,
  StyleSheet,
  Text,
  View
} from 'react-native';

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' +
    'Cmd+D or shake for dev menu',
  android: 'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

type Props = {};
export default class App extends Component<Props> {
  _press(){
		NativeModules.ToastExample.show('Awesome');
		play('rtsp://admin:wnvxiaoti5566123@10.17.5.99:554/Streaming/Channels/101');
	}
  _press_play(){
	  this.video.play();
  }
  
  _press_savePic(){
	  this.video.pause();
  }
  
  _press_full_screen(){
	  
  }
  
  
  render() {
	//const _this = this;
    return (
      <View style={styles.container}>
        <Text style={styles.welcome} onPress={this._press}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit App.js
        </Text>
		<VideoView 
			ref={(video)=>{this.video = video}}
			style={styles.player}
			url={'rtsp://admin:wnvxiaoti5566123@10.17.5.99:554/Streaming/Channels/101'}>
		</VideoView>
		<Text style={styles.instructions}>
          {instructions}
        </Text>
		<Text style={styles.instructions} onPress={this._press_play}>
          播放
        </Text>
		<Text style={styles.instructions} onPress={this._press_savePic}>
          暂停
        </Text>
		<Text style={styles.instructions}>
          錄屏
        </Text>
		<Text style={styles.instructions} onPress={this._press_full_screen}>
          全屏
        </Text>
		
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  player:{
    width:300,
    height:200,
  },
});
