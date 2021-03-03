# audio-only-server

Just kidding, it's serverless ;)

There are two functions here, designed to be deployed as [Netlify functions](https://www.netlify.com/products/functions/)

You can deploy to your account via the deploy button below (including the React app):

[![Deploy with Netlify](https://www.netlify.com/img/deploy/button.svg)](https://app.netlify.com/start/deploy?repository=https://github.com/daily-demos/party-line)

You will be prompted to add your API key, which you can get from the [Developers](https://dashboard.daily.co/developers) section of the Dashboard. Sign up for a free account if you don't have one already.

## Functions

`room.js` - This accepts an empty `POST` and calls the [`/rooms`](https://docs.daily.co/reference#rooms) endpoint, with a few properties, feel free to change these in your own implementation. We're also calling [`/meeting-tokens`](https://docs.daily.co/reference#meeting-tokens) since you'll want at least one token when the room is initially created. This saves the client from doing two API calls.

`token.js` - This accepts a `POST` with a body in the form of `{ properties: { room_name: INSERTNAME } }` and calls the [`/meeting-tokens`](https://docs.daily.co/reference#meeting-tokens) endpoint to create an owner token for the `room_name` specified.
