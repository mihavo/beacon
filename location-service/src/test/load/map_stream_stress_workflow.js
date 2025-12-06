import http from "k6/http";
import {vu} from "k6/execution";
import sse from 'k6/x/sse';
import papaparse from "https://jslib.k6.io/papaparse/5.1.1/index.js";
import {SharedArray} from "k6/data";

const baseUrl = "http://localhost:8080";
const testUrl = baseUrl + "/maps";
const authUrl = baseUrl + "/auth/login";

const users_limit = 100;

const bbox = {
  minLon: __ENV.MIN_LON || '1.5',
  maxLon: __ENV.MAX_LON || '2.8',
  minLat: __ENV.MIN_LAT || '45.3',
  maxLat: __ENV.MAX_LAT || '50.3',
};

const filePath = './data/creds.csv'

const users = new SharedArray("Logins", function () {
  return papaparse.parse(open(filePath), {header: true}).data.slice(0, users_limit);
});

export const options = {
  vus: users.length, duration: '120s'
};

export function setup() {
  return users.map((user) => {
    console.log("Authenticating VU" + vu.idInTest + " / username: ", user.username);
    const res = http.post(authUrl, JSON.stringify({
      username: user.username, password: user.password,
    }), {headers: {"Content-Type": "application/json"}},);

    if (res.status !== 200) {
      throw new Error(`Failed to login user ${user.username}, reason:  ${res.body}`,);
    }
    console.log(`Authenticated User:  ${user.username}`);
    return {
      ...user, token: res.json("token"),
    };
  });
}

export default function (data) {
  const user = data[__VU - 1];
  console.log("Executing for VU: " + vu.idInTest + " / username: ", user.username);

  const params = {
    headers: {
      "Content-Type": "application/json", Authorization: `Bearer ${user.token}`,
    },
  };

  const url = `${testUrl}/subscribe?minLon=${bbox.minLon}&maxLon=${bbox.maxLon}&minLat=${bbox.minLat}&maxLat=${bbox.maxLat}`;

  const response = sse.open(url, params, function (client) {
    client.on('open', function open() {
      console.log('connected')
    })

    client.on('event', function (event) {
      console.log(`event id=${event.id}, name=${event.name}, data=${event.data}`)
      if (parseInt(event.id) === 4) {
        client.close()
      }
    })

    client.on('error', function (e) {
      console.log('An unexpected error occurred: ', e.error())
    })
  })

}