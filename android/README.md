# Party Line

This is the {Java|Kotlin} version of Party Line, an Android (webrtc) audio social networking demo app via daily-js [link to daily-js]

<img src="https://user-images.githubusercontent.com/885084/109444950-1e9ffc00-7a47-11eb-848a-9bda69258744.png" width="25%">

## Run on Device

To run the app, clone, open the project in Android Studio, and hit run

## Run on Emulator

First make sure microphone is supported then check OS specific instructions below

<img src="https://user-images.githubusercontent.com/885084/109444441-eb10a200-7a45-11eb-9068-8a9179ab467e.png" width="35%">

### Linux

Running on Linux works fine as long as you set targetSdkVersion to 29 or below. Setting targetSdkVersion to 30 might cause selinux to prevent the emulator from communicating via sockets. Follow these steps if you have this issue https://source.android.com/security/selinux/validate. Tested on Ubuntu 20.04.

### macOS

I still need to test Android Studio emulator on macOS. Will report here if it works out-of-the-box for targetSdkVersion 30

## Implementation

### Webrtc

[I can go into as much detail as we want here with code snippets] The app uses the daily-js api to implement webrtc in a WebView, and a javascript bridge (`WebAppInterface`) to android. A hidden WebView loads the daily-js library and adds a thin wrapper to interface with android.

```java
// RoomFragment.java

final class WebAppInterface {

    @JavascriptInterface
    public void joinedMeeting(String Id) {
    ...
}

binding.webview.addJavascriptInterface(new WebAppInterface(), "Android");
binding.webview.loadUrl("file:///android_asset/daily-js.html");
```

```javascript
// daily-js.html

<head>
    <script src="https://unpkg.com/@daily-co/daily-js"></script>
</head>

<script>
    call.on('joined-meeting', () => {
        console.log('[JOINED MEETING]');
        Android.joinedMeeting(call.participants().local.session_id)
    });
    ...
<script>
```

### Android Jetpack

For the most part the project is built on top of Android Jetpack libraries from the ground up

* AndroidX
* View Binding
* RecyclerView and ConcatAdapter
* Navigation component and Directions
* Safe Args and Actions

## TODO

- [x] Convert to Kotlin [link to kotlin repo?] (if this is the java repo)
- [ ] Migrate to Data Binding
- [ ] Migrate `ParticipantsAdapter.java` to View Binding
