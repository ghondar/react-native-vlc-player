//VlcPlayerView

import PropTypes from 'prop-types'
import React,{Component} from 'react';
import {
    requireNativeComponent,
    View,
    UIManager,
    findNodeHandle,
} from 'react-native';

var RCT_VIDEO_REF = 'VideoView';

class VideoView extends Component{
	
	constructor(props){
		super(props);
		this.refs[RCT_VIDEO_REF] = this.refs[RCT_VIDEO_REF].bind(this);
	}
	
	play(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.refs[RCT_VIDEO_REF]),
			UIManager.VideoView.Command.play,
			null
		);
	}
	
	pause(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.refs[RCT_VIDEO_REF]),
			UIManager.VideoView.Command.pause,
			null
		);
	}
	
	full_screen(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.refs[RCT_VIDEO_REF]),
			UIManager.VideoView.Command.full_screen,
			null
		);
	}
	
	capture(){
		UIManager.dispatchViewManagerCommand(
			findNodeHandle(this.refs[RCT_VIDEO_REF]),
			UIManager.VideoView.Command.capture,
			null
		);
	}
	
	render(){
        return <RCTVideoView
            {...this.props}
            ref = {RCT_VIDEO_REF}
        />;
    };
}

VideoView.name = "VideoView";
VideoView.propTypes = {
	style: PropTypes.style,
	url : PropTypes.string,
};

var RCTVideoView = requireNativeComponent('VideoView',VideoView,{
    nativeOnly: {onChange: true}
});
module.exports = VideoView;


// var oface = {
	// name : 'VlcPlayerView',
	// propTypes : {
		// url : PropTypes.string,
	// },
// }


// module.exports = requireNativeComponent('VlcPlayerView', oface);