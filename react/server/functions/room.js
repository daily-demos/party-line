const { apiHelper } = require("../api-helper");

const headers = {
  "Content-Type": "application/json; charset=utf-8",
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "Content-Type",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE",
};

exports.handler = async function (event, context) {
  if (event.httpMethod !== "POST") {
    return {
      statusCode: 200,
      headers,
      body: "This was not a POST request!",
    };
  }

  let res = {};
  let code = 500;
  try {
    const roomBody = JSON.stringify({
      properties: {
        // expire in 10 minutes
        exp: Math.round(Date.now() / 1000) + 10 * 60,
        eject_at_room_exp: true,
        signaling_impl: "ws",
      },
    });
    const room = await apiHelper("post", "/rooms", roomBody);
    res = room;

    const tokenBody = JSON.stringify({
      properties: {
        // expire in 10 minutes
        exp: Math.round(Date.now() / 1000) + 10 * 60,
        room_name: room.name,
        is_owner: true,
      },
    });
    const token = await apiHelper("post", "/meeting-tokens", tokenBody);

    res = { token: token.token, ...res };
    code = 200;
  } catch (e) {
    console.log("error: ", e);
    res = { error: e.message };
    code = 500;
  }

  return {
    statusCode: code,
    body: JSON.stringify(res),
    headers,
  };
};
