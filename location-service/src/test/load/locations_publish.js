import http from "k6/http";
import {sleep} from "k6";
import {vu} from "k6/execution";
import papaparse from "https://jslib.k6.io/papaparse/5.1.1/index.js";
import {SharedArray} from "k6/data";

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

const clusters = [// { lat: 23.285, lon: -159.244 }, // Hawaii
  { lat: 48.8566, lon: 2.3522 }, // Europe (Paris)
  // { lat: 34.0479, lon: 100.6197 }, // Asia (China center)
  // { lat: -72.215, lon: -4.094 }, // South America
];

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
      ...user, token: res.json("token"), baseCoords: getRandomClusterCoordinates()
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

  let coords = user.baseCoords;
  const captures = [];
  const baseTimestamp = new Date(new Date().getTime() - 24 * 60 * 60 * 1000); //yesterday, same time
  for (let i = 0; i < 15; i++) {
    coords = moveInDirection(coords);
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

function getRandomClusterCoordinates() {
  const cluster = clusters[Math.floor(Math.random() * clusters.length)];
  return { latitude: cluster.lat, longitude: cluster.lon };
}

function getCloseCoordinates(baseCoords) {
  const latitude = baseCoords.latitude + (Math.random() - 0.5) * 0.5; // ± ~0.5 km
  const longitude = baseCoords.longitude + (Math.random() - 0.5) * 0.5; // ± ~0.5 km
  return { latitude, longitude };
}

let currentBearing = Math.random() * 360;

function moveInDirection(baseCoords, distanceMeters = 5) {
  const earthRadius = 6371000; // meters
  const bearingRad = (currentBearing * Math.PI) / 180;
  const lat1 = (baseCoords.latitude * Math.PI) / 180;
  const lon1 = (baseCoords.longitude * Math.PI) / 180;

  // Calculate new lat/lon using the haversine formula
  const lat2 = Math.asin(Math.sin(lat1)
      * Math.cos(distanceMeters / earthRadius)
      + Math.cos(lat1)
      * Math.sin(distanceMeters / earthRadius)
      * Math.cos(bearingRad));
  const lon2 = lon1 + Math.atan2(Math.sin(bearingRad)
      * Math.sin(distanceMeters / earthRadius)
      * Math.cos(lat1), Math.cos(distanceMeters / earthRadius) - Math.sin(lat1) * Math.sin(lat2));

  // Convert back to degrees
  const newLat = (lat2 * 180) / Math.PI;
  const newLon = (lon2 * 180) / Math.PI;

  // Slightly change the bearing to make it look more natural (curves)
  currentBearing += (Math.random() - 0.5) * 10; // small turn up to ±5°
  if (currentBearing < 0) currentBearing += 360;
  if (currentBearing >= 360) currentBearing -= 360;

  return {latitude: newLat, longitude: newLon};
}