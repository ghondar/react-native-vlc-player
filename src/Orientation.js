import { DeviceEventEmitter, NativeModules } from 'react-native'

const { Orientation } = NativeModules

const META = '__listener_id'
const orientationDidChangeEvent = 'orientationDidChange'
const specificOrientationDidChangeEvent = 'specificOrientationDidChange'

let id = 0
let listeners = {}

function getKey(listener) {
  if(!listener.hasOwnProperty(META)) {
    if(!Object.isExtensible(listener))
      return 'F'

    Object.defineProperty(listener, META, {
      value: 'L' + ++id
    })
  }

  return listener[META]
};

export const DeviceOrientation = {
  HideNavigationBar() {
    if(Platform.OS === 'android')
      Orientation.HideNavigationBar()
    else
      return false
  },

  ShowNavigationBar() {
    if(Platform.OS === 'android')
      Orientation.ShowNavigationBar()
    else
      return false
  },

  getOrientation(cb) {
    Orientation.getOrientation((error,orientation) =>{
      cb(error, orientation)
    })
  },

  getSpecificOrientation(cb) {
    Orientation.getSpecificOrientation((error,orientation) =>{
      cb(error, orientation)
    })
  },

  lockToPortrait() {
    Orientation.lockToPortrait()
  },

  lockToLandscape() {
    Orientation.lockToLandscape()
  },

  lockToLandscapeRight() {
    Orientation.lockToLandscapeRight()
  },

  lockToLandscapeLeft() {
    Orientation.lockToLandscapeLeft()
  },

  unlockAllOrientations() {
    Orientation.unlockAllOrientations()
  },

  addOrientationListener(cb) {
    var key = getKey(cb)
    listeners[key] = DeviceEventEmitter.addListener(orientationDidChangeEvent,
      (body) => {
        cb(body.orientation)
      })
  },

  removeOrientationListener(cb) {
    var key = getKey(cb)

    if(!listeners[key])
      return

    listeners[key].remove()
    listeners[key] = null
  },

  addSpecificOrientationListener(cb) {
    var key = getKey(cb)

    listeners[key] = DeviceEventEmitter.addListener(specificOrientationDidChangeEvent,
      (body) => {
        cb(body.specificOrientation)
      })
  },

  removeSpecificOrientationListener(cb) {
    var key = getKey(cb)

    if(!listeners[key])
      return

    listeners[key].remove()
    listeners[key] = null
  },

  getInitialOrientation() {
    return Orientation.initialOrientation
  }
}
