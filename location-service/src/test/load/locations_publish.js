export const options = {
  iterations: 50,
};

const baseUrl = "localhost:8080";
const url = baseUrl + "/locations";

const token = __ENV.TOKEN;

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
  const payload = {
    latitude: coords.latitude,
    longitude: coords.longitude,
    timestamp: Date.now(),
  };
  const res = http.post(url, payload, params);
  console.log(res);
}

function getRandomCoordinates() {
  const lat = (Math.random() * 180 - 90).toFixed(6);
  const lon = (Math.random() * 360 - 180).toFixed(6);
  return { latitude: parseFloat(lat), longitude: parseFloat(lon) };
}
