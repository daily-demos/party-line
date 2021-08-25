# Party line: An audio-only demo for React Native

## Built by and with [Daily](https://docs.daily.co/reference#using-the-react-native-daily-js-library)

---

This app is built using the React Native CLI.

To run locally:

- Clone this repo locally
- Run the following

```terminal
npm i
npx pod-install
npx react-native start
```

### Testing on an Android device

Connect your device and run the following:

```terminal
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

## Additional links

- [react-native-daily-js README](https://www.npmjs.com/package/@daily-co/react-native-daily-js)
- [react-native-daily-js docs](https://docs.daily.co/reference#using-the-react-native-daily-js-library)

## Deployment

Once you've deployed your own server, make sure to update the necessary URLs as noted in:

- react-native/contexts/CallProvider.jsx

---

## Related blog posts/tutorials

Learn more about this demo on the [Daily blog](https://www.daily.co/blog/.

- [Introduction to Party Line and its feature specs](https://www.daily.co/blog/how-to-build-a-billion-dollar-audio-app-in-a-weekend/)
- [Create audio-only meetings with Daily](https://www.daily.co/blog/create-audio-only-meetings-with-daily/)
- [Audio-only app use cases](https://www.daily.co/blog/audio-only-social-networks-what-are-they-and-how-are-they-being-used/)
