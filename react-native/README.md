# Party line: an audio-only demo for React Native

## Built by and with [Daily](https://docs.daily.co/reference#using-the-react-native-daily-js-library)

---

This app is built using the React Native CLI.

To run locally:

- Clone this repo locally
- Run the following

```
npm i
npx pod-install
npx react-native start
```

### Testing on an Android device

Connect your device and run the following:

```
npx react-native run-android
```

Alternatively, to run in Android Studio, open the `android` directory in Android Studio, make sure your device is selected in the AVD Manager as the target device, and click the `Run` button.

### Testing on an iOS device

First, open `AudioOnlyRN.xcworkspace` in Xcode. Under `Signing & Capabilities` make sure a team is selected.

Connect your device, select your device as the target device and, click the `Run` button.

_Note:_ The first time you run the app on an iPhone you will need to add the app to your trusted devices. To do this, go to:
`General > Device Management > Apple Development > Trust app`

## Testing

To add more participants to your call, you can take advantage of the React demo currently hosted at [https://partyline.daily.co/](https://partyline.daily.co/).

## Additional links:

- [react-native-daily-js README](https://www.npmjs.com/package/@daily-co/react-native-daily-js)
- [react-native-daily-js docs](https://docs.daily.co/reference#using-the-react-native-daily-js-library)
