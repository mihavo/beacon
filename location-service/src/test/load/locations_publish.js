import http from "k6/http";
import { sleep } from "k6";
import { vu } from "k6/execution";
import papaparse from "https://jslib.k6.io/papaparse/5.1.1/index.js";
import { SharedArray } from "k6/data";

const baseUrl = "http://localhost:8080";
const testUrl = baseUrl + "/locations";
const authUrl = baseUrl + "/auth/login";

// AUTH Method 1: Single User token from environment
// const token = __ENV.AUTH_TOKEN;
//
// if (!token) {
//   throw new Error("AUTH_TOKEN environment variable is not set!");
// }

//AUTH Method 2: Multiple Users with creds from file in data/creds.csv
const users = new SharedArray("Logins", function () {
  return papaparse.parse(open("./data/creds.csv"), { header: true }).data;
});

export const options = {
  vus: users.length,
  iterations: 20, // 10 iterations per VU
};

export function setup() {
  return users.map((user) => {
    console.log(
      "Authenticating VU" + vu.idInTest + " / username: ",
      user.username,
    );
    const res = http.post(
      authUrl,
      JSON.stringify({
        username: user.username,
        password: user.password,
      }),
      { headers: { "Content-Type": "application/json" } },
    );

    if (res.status !== 200) {
      throw new Error(
        `Failed to login user ${user.username}, reason:  ${res.body}`,
      );
    }
    console.log(`Authenticated User:  ${user.username}`);
    return {
      ...user,
      token: res.json("token"),
    };
  });
}

export default function (data) {
  const user = data[__VU - 1];
  console.log(
    "Executing for VU: " + vu.idInTest + " / username: ",
    user.username,
  );

  const params = {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${user.token}`,
    },
  };

  const baseCoords = getRandomCoordinates();
  const captures = [];
  const baseTimestamp = new Date(new Date().getTime() - 24 * 60 * 60 * 1000); //yesterday, same time
  for (let i = 0; i < 15; i++) {
    const coords = getCloseCoordinates(baseCoords);
    const timestamp = new Date(baseTimestamp.getTime() + i * 5000);
    captures.push({
      coords,
      capturedAt: timestamp.toISOString(),
    });
  }
  const payload = JSON.stringify(captures);
  const res = http.post(testUrl, payload, params);

  console.log(`Status: ${res.status} Body: ${res.body}`);

  sleep(2 + Math.random() * 5);
}

function getRandomCoordinates() {
  const lat = (Math.random() * 180 - 90).toFixed(6);
  const lon = (Math.random() * 360 - 180).toFixed(6);
  return { latitude: parseFloat(lat), longitude: parseFloat(lon) };
}

function getCloseCoordinates(baseCoords) {
  const latitude = baseCoords.latitude + (Math.random() - 0.5) * 0.002; // ± ~100m
  const longitude = baseCoords.longitude + (Math.random() - 0.5) * 0.002; // ± ~100m
  return { latitude, longitude };
}
