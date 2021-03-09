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

  const reqBody = JSON.parse(event.body);
  const roomName = reqBody.properties.room_name;

  let res = {};
  let code = 500;
  try {
    const tokenBody = JSON.stringify({
      properties: {
        // expire in 10 minutes
        exp: Math.round(Date.now() / 1000) + 10 * 60,
        room_name: roomName,
        is_owner: true,
      },
    });
    const token = await apiHelper("post", "/meeting-tokens", tokenBody);

    res = token;
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
