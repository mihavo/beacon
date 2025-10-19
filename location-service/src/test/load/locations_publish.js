import http from "k6/http";
import { sleep } from "k6";

export const options = {
  vus: 10,
  duration: "20s",
};

const baseUrl = "http://localhost:8080";
const url = baseUrl + "/locations";

const token = __ENV.AUTH_TOKEN;

if (!token) {
  throw new Error("AUTH_TOKEN environment variable is not set!");
}

export default function () {
  const params = {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  };

  const baseCoords = getRandomCoordinates();
  const captures = [];
  const baseTimestamp = new Date();
  for (let i = 0; i < 15; i++) {
    const coords = getCloseCoordinates(baseCoords);
    const timestamp = new Date(baseTimestamp.getTime() + i * 5000);
    captures.push({
      coords,
      capturedAt: timestamp.toISOString(),
    });
  }
  const payload = JSON.stringify(captures);
  const res = http.post(url, payload, params);

  console.log(`Status: ${res.status} Body: ${res.body}`);

  sleep(5 + Math.random() * 5);
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
