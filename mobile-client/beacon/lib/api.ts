import axios from "axios";
import {getToken} from "@/app/context/AuthContext";
import {
    AcceptFriendRequest,
    AcceptFriendResponse,
    ConnectResponse,
    DeclineFriendRequest,
    DeclineFriendResponse,
    DeleteFriendResponse,
    GetConnectionsResponse,
    GetFriendsResponse
} from "@/types/Connections";

const BASE = process.env.EXPO_PUBLIC_API_URL;

const api = axios.create({
    baseURL: BASE,
})

api.interceptors.request.use(
    async (config) => {
        const token = await getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export async function login(username: string, password: string) {

    const res = await fetch(`auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
        body: JSON.stringify({username, password}),
  });
    return await res.json();
}

export async function register(dto: any) {
    const res = await fetch(`auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
    return await res.json();
}

export async function getFriends() {
    const res = await axios.get<GetFriendsResponse>(`users/connections/friends`);
    return res.data;
}

export async function getConnections() {
    const res = await axios.get<GetConnectionsResponse>(`users/connections`);
    return res.data;
}

export async function connect(id: string) {
    const res = await axios.post<ConnectResponse>(`users/connections/${id}`);
    return res.data;
}

export async function removeFriend(id: string) {
    const res = await axios.delete<DeleteFriendResponse>(`users/connections/${id}`);
    return res.data;
}

export async function acceptFriendRequest(request: AcceptFriendRequest) {
    const res = await axios.post<AcceptFriendResponse>(`users/connections/accept`, request);
    return res.data;
}

export async function declineFriendRequest(request: DeclineFriendRequest) {
    const res = await axios.post<DeclineFriendResponse>(`users/connections/decline`, request);
    return res.data;
}