# Party line: An audio-only demo for Android

## Built by and with [Daily](https://docs.daily.co/reference#using-the-react-native-daily-js-library)

---

## Prerequisites

- clone this repo locally
- Download and install the latest version of Android Studio
- In Android Studio, Select Open an Existing Project. The cloned repository contains both Kotlin and Java projects under the android folder. Choose the project you want.

## Running on and Android device

Once the project is loaded and Gradle sync is complete, connect your device. Make sure it is selected in the "Availale devices" menu and hit Run.

## Running on an Emulator

If you created an emulator in AVD manager, you can select it as a target to run the app. Once the emulator is running, make sure  micrphone support is enabled like the image below

<img src="https://user-images.githubusercontent.com/885084/109444441-eb10a200-7a45-11eb-9068-8a9179ab467e.png" width="35%">

### Linux

Running the app on an emulator on a Linux machine works out-of-the-box as long as you set `targetSdkVersion` to `<=29`. Setting `targetSdkVersion` to `30` might cause selinux to prevent the emulator from communicating via sockets. Follow these steps if you set `targetSdkVersoin 30` and encounter this issue https://source.android.com/security/selinux/validate (Tested on Ubuntu 20.04). Currently `targetSdkVersion 29` is set by default.

## Testing

To add more participants to your call, you can take advantage of the React demo currently hosted at [https://audio-only-react.netlify.app/](https://audio-only-react.netlify.app/).
