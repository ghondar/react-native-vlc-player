import React, { Component , Fragment } from 'react'
import PropTypes from 'prop-types'
import {
  View,
  Dimensions,
  StatusBar
} from 'react-native'

import Controls from './src/Controls'

const screenWidth = Math.round(Dimensions.get('window').width)
const screenHeight = Math.round(Dimensions.get('window').height)

import NativeVlcPlayer from './src/NativeVlcPlayer'
import * as Orientation from './src/Orientation'

export const DeviceOrientation = Orientation.DeviceOrientation

class VlcPlayer extends Component {
  static propTypes = {
    style: PropTypes.shape({
      width : PropTypes.number.isRequired,
      height: PropTypes.number.isRequired
    }),
    paused: PropTypes.bool
  }

  static defaultProps = {
    style: {
      width : 0,
      height: 0
    },
    paused: false
  }

  state = {
    hidden  : false,
    width   : this.props.style.width,
    height  : this.props.style.height,
    controls: {
      toggle: 'md-expand',
      pause : this.props.paused
    },
    overlay: {
      opacity        : 0,
      backgroundColor: 'transparent'
    }
  };

  shouldComponentUpdate(nextProps) {
    if(this.props.style.width !== nextProps.style.width || this.props.style.height !== nextProps.style.height)
      this.setState({
        width : nextProps.style.width,
        height: nextProps.style.height
      })

    if(this.props.paused !== nextProps.paused)
      this.setState(({ controls }) => ({
        controls: {
          ...controls,
          pause: nextProps.paused
        }
      }))

    return true
  }

  componentDidMount() {
    DeviceOrientation.addOrientationListener(this._orientationDidChange)
  }

  _orientationDidChange = orientation => {
    const { controls } = this.state

    if(orientation === 'LANDSCAPE') {
      this.setState({
        hidden: true,
        width : screenHeight,
        height: screenWidth,
        toggle: controls.toggle
      })
      DeviceOrientation.HideNavigationBar()
    } else {
      this.setState({
        hidden        : false,
        width         : this.props.style.width,
        height        : this.props.style.height,
        toggle        : controls.toggle,
        overlayTimeout: null
      })
      DeviceOrientation.ShowNavigationBar()
    }
  };

  changeOverlay = () => {
    const { overlayTimeout } = this.state

    clearTimeout(overlayTimeout)

    this.setState({
      overlay: {
        opacity        : 0.6,
        backgroundColor: 'black'
      },
      overlayTimeout: setTimeout(() => {
        this.setState({
          overlay: {
            opacity        : 0,
            backgroundColor: 'transparent'
          },
          overlayTimeout: null
        })
      }, 3000)
    })
  };

  render() {
    const { forwardRef, style, ...rest  } = this.props
    const { overlay, controls, width, height } = this.state

    const props = JSON.parse(JSON.stringify(rest))

    return (
      <Fragment>
        <StatusBar hidden={this.state.hidden} />
        <View
          style={{
            position: 'relative'
          }}>
          <NativeVlcPlayer
            {...props}
            paused={controls.pause}
            ref={forwardRef}
            style={{
              ...style,
              width,
              height
            }} />

          <Controls
            controls={controls}
            height={this.state.height}
            onOrientation={this._handleOrientation}
            onOverlay={this._handleOverlay}
            onPlay={this._handlePlay}
            overlay={overlay}
            width={this.state.width} />

        </View>
      </Fragment>
    )
  }

  _handlePlay = () => {
    this.changeOverlay()
    this.setState(({ controls }) => ({
      controls: {
        ...controls,
        pause: !controls.pause
      }
    }))
  };

  _handleOverlay = () => {
    const { overlayTimeout } = this.state
    if(!overlayTimeout) {
      this.changeOverlay()
    } else {
      clearInterval(overlayTimeout)
      this.setState({
        overlay: {
          opacity        : 0,
          backgroundColor: 'transparent'
        },
        overlayTimeout: null
      })
    }
  };

  _handleOrientation = () => {
    this.changeOverlay()
    DeviceOrientation.getOrientation((err, orientation) => {
      if(orientation === 'PORTRAIT')
        DeviceOrientation.lockToLandscape()
      else
        DeviceOrientation.lockToPortrait()
    })
  };
}

export default React.forwardRef((props, ref) => (
  <VlcPlayer {...props} forwardRef={ref} />
))
