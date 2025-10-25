import http from "k6/http";
import {check, sleep} from "k6";
import faker from "k6/x/faker";

const LOCATIONS_PER_USER = 10;

export let options = {
  vus: 50,
  duration: "2m",
  thresholds: {
    http_req_duration: ["p(95)<500"],
  },
};

const baseUrl = "http://localhost:8080";
const testUrl = baseUrl + "/locations";
const registerUrl = baseUrl + "/auth/register";

const clusters = [
  { lat: 23.285, lon: -159.244 }, // Hawaii
  { lat: 48.8566, lon: 2.3522 }, // Europe (Paris)
  { lat: 34.0479, lon: 100.6197 }, // Asia (China center)
  { lat: -72.215, lon: -4.094 }, // South America
];

export function setup() {
  const users = [];

  for (let i = 0; i < options.vus; i++) {
    const username = faker.internet.username();
    const fullName = `${faker.person.firstName()} ${faker.person.lastName()}`;
    const password = faker.internet.password(
      true,
      false,
      true,
      true,
      false,
      12,
    );

    const res = http.post(
      registerUrl,
      JSON.stringify({ username, fullName, password }),
      { headers: { "Content-Type": "application/json" } },
    );

    if (res.status === 201) {
      const token = res.json("token");
      users.push({
        username,
        token,
        baseCoords: getRandomClusterCoordinates(),
      });
      console.log(`Registered ${username}`);
    } else {
      console.error(
        `Failed to register user ${username}: ${res.status} ${res.body}`,
      );
    }
  }

  return { users };
}

export default function (data) {
  const user = data.users[__VU - 1];
  const { username, token, baseCoords } = user;
  const baseTimestamp = new Date(new Date().getTime() - 24 * 60 * 60 * 1000); //yesterday, same time
  const captures = [];
  const params = {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  };

  for (let i = 0; i < LOCATIONS_PER_USER; i++) {
    const coords = getCloseCoordinates(baseCoords);
    const timestamp = new Date(baseTimestamp.getTime() + i * 5000);
    captures.push({
      coords,
      capturedAt: timestamp.toISOString(),
    });
  }

  const payload = JSON.stringify(captures);
  const locRes = http.post(testUrl, payload, params);

  const locationPublished = check(locRes, {
    "location published": (r) => r.status === 200,
  });

  if (locationPublished) {
    console.log(`Location published successfully for user ${username}`);
  } else {
    console.log(
      `Failed to publish location for user ${username}. Status: ${locRes.status} Message: ${locRes.message}`,
    );
  }
  sleep(0.1);

  sleep(1);
}

function getRandomClusterCoordinates() {
  const cluster = clusters[Math.floor(Math.random() * clusters.length)];
  return { latitude: cluster.lat, longitude: cluster.lon };
}

function getCloseCoordinates(baseCoords) {
  const latitude = baseCoords.latitude + (Math.random() - 0.5) * 0.5; // + or - ~0.5 km
  const longitude = baseCoords.longitude + (Math.random() - 0.5) * 0.5; // + or - ~0.5 km
  return { latitude, longitude };
}
