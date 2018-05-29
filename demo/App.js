import React, { Component } from 'react';
import {
  View
} from 'react-native';
import Player from '../Player';

export default class Page extends Component {
  constructor(props) {
    super(props);

    this.playerPause = this.playerPause.bind(this);
    this.playerRecord = this.playerRecord.bind(this);
    this.playerCapture = this.playerCapture.bind(this);
    this.playerFullScreen = this.playerFullScreen.bind(this);
  }

  playerPause() {
    this.player.play();
  }

  playerRecord() {
    // TODO
  }

  playerCapture() {
    this.player.capture();
  }

  playerFullScreen() {
    this.player.full_screen();
  }


  render() {
    return (
      <View>
        <Player
          ref={ player => this.player = player }
      		url={'rtsp://admin:wnvxiaoti5566123@10.17.5.99:554/Streaming/Channels/101'}
        />
      </View>
    );
  }
}
