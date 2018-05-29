import PropTypes from 'prop-types'
import React,{Component} from 'react';
import {
    requireNativeComponent,
    View,
    UIManager,
    findNodeHandle,
} from 'react-native';

class VideoView extends Component{
	
	constructor(props){
		super(props);

    this.play = this.play.bind(this);
    this.pause = this.pause.bind(this);
    this.full_screen = this.full_screen.bind(this);
    this.capture = this.capture.bind(this);
	}

	play(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this),
			UIManager.VideoView.Commands.play,
			null
		);
	}
	
	pause(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this),
			UIManager.VideoView.Commands.pause,
			null
		);
	}
	
	full_screen(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this),
			UIManager.VideoView.Commands.full_screen,
			null
		);
	}
	
	capture(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this),
			UIManager.VideoView.Commands.capture,
			null
		);
	}
	
	render(){
    return <RCTVideoView
      {...this.props}
    />;
  };
}

var RCTVideoView = requireNativeComponent('VideoView',VideoView,{
    nativeOnly: {onChange: true}
});
module.exports = VideoView;
