# Party line: An audio-only demo for iOS

## Built by and with [Daily](https://docs.daily.co/reference)

---

This showcases a basic but complete audio chat app using [SwiftUI](https://developer.apple.com/xcode/swiftui/).

## Prerequisites

- Download and install Xcode, either via the [App Store](https://apps.apple.com/us/app/xcode/id497799835?mt=12), or the [Apple Developer](https://developer.apple.com/download/) site. It's quite a big download, so start downloading it now and just start spelunking in the repo.
- You will also need an actual iPhone connected to your Mac, or simulate the app using the Xcode Simulator.

## Running locally

1. Clone this repo locally
2. Open `/ios/Party Line.xcodeproj` in Xcode
3. Connect an iPhone via USB and choose it as deployment target
4. Choose a build destination via "Main Menu > Product > Destination > iOS Device > …", or an iPhone simulator of your choice
5. Run via "Main Menu > Product > Run", or the corresponding toolbar item

## Testing

To add more participants to your call, you can take advantage of the React demo currently hosted at [https://partyline.daily.co/](https://partyline.daily.co/).

## Deployment

Once you've deployed your own server, make sure to update the necessary URLs as noted in:

- ios/Party Line/API/Client.swift
