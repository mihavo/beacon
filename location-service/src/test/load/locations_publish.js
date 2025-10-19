import http from "k6/http";

export const options = {
  iterations: 40,
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

  const coords = getRandomCoordinates();
  const payload = JSON.stringify([
    {
      coords: {
        latitude: coords.latitude,
        longitude: coords.longitude,
      },
      capturedAt: new Date().toISOString(),
    },
  ]);
  console.log(JSON.parse(payload)[0].capturedAt);
  const res = http.post(url, payload, params);
  console.log(`Status: ${res.status} Body: ${res.body}`);
}

function getRandomCoordinates() {
  const lat = (Math.random() * 180 - 90).toFixed(6);
  const lon = (Math.random() * 360 - 180).toFixed(6);
  return { latitude: parseFloat(lat), longitude: parseFloat(lon) };
}
