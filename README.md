
# react-native-upnp

## Getting started

`$ npm install react-native-upnp --save`

### Mostly automatic installation

`$ react-native link react-native-upnp`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-upnp` and add `RNUpnp.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNUpnp.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNUpnpPackage;` to the imports at the top of the file
  - Add `new RNUpnpPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-upnp'
  	project(':react-native-upnp').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-upnp/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-upnp')
  	```


## Usage
```javascript
import RNUpnp from 'react-native-upnp';

// TODO: What to do with the module?
RNUpnp;
```
  