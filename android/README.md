# Party line: An audio-only demo for Android

## Built by and with [Daily](https://docs.daily.co/docs/reference-docs)

---

## Prerequisites

- Clone this repo locally
- Download and install the latest version of Android Studio
- In Android Studio, Select "Open an Existing Project". The cloned repository contains both kotlin and java projects under the android folder. Choose the project you want.

## Running on an Android device

Once the project is loaded and gradle sync is complete, connect your device. Make sure it is selected in the "Available devices" menu and hit Run.

## Running on an Emulator

If you created an emulator with avd manager, you can select it from the "Available" devices menu and hit Run. Once the emulator is running, make sure microphone support is enabled like the image below

<img src="https://user-images.githubusercontent.com/885084/109444441-eb10a200-7a45-11eb-9068-8a9179ab467e.png" width="35%">

### targetSdkVersion 30

Running the app on an emulator out-of-the-box as long as you set `targetSdkVersion` to `<=29`. Setting `targetSdkVersion` to `30` might cause Linux to prevent the emulator from communicating via sockets. Follow [these steps](https://source.android.com/security/selinux/validate) if you want to set `targetSdkVersion 30` and encounter this issue (tested on Ubuntu 20.04). Currently `targetSdkVersion 29` is set by default.

## Testing

To add more participants to your call, you can take advantage of the React demo currently hosted at [https://partyline.daily.co/](https://partyline.daily.co/).

## Deployment

Once you've deployed your own server, make sure to update the necessary URLs as noted in:

- android/java/app/src/main/assets/audio-single-file.html
- android/kotlin/app/src/main/assets/audio-single-file.html
- android/kotlin/app/src/main/java/com/daily/partyline/WebAppClient.kt
- android/java/app/src/main/java/com/daily/partyline/WebAppClient.java
