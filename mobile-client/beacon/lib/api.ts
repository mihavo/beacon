import axios from "axios";
import {getToken, logoutRef} from "@/app/context/AuthContext";
import {
    AcceptFriendRequest,
    AcceptFriendResponse,
    ConnectResponse,
    DeclineFriendRequest,
    DeclineFriendResponse,
    DeleteFriendResponse,
    GetConnectionsResponse,
    GetFriendsResponse,
    GetUserResponse
} from "@/types/Connections";
import {Alert} from "react-native";
import {BoundingBox, MapSnapshotResponse, SendBatchedLocationsRequest} from "@/types/Map";
import {router} from "expo-router";
import {Geofence} from "@/types/Geofence";
import {AnalyticsLocationPoint, LocationPoint, TimeRangeOptions} from "@/types/History";

export const BASE = process.env.EXPO_PUBLIC_API_URL;

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

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            console.log(error.response?.data?.message);
            await logoutRef.current?.();
            return router.replace('/auth/login');
        }

        let message = "Something went wrong.";
        if (error.response?.data?.message) {
            message = error.response.data.message;
        } else if (error.message) {
            message = error.message;
        }
        Alert.alert("Error", message);
        return Promise.reject(error);
    }
);

export async function login(username: string, password: string) {
    const res = await api.post<any>('auth/login', {
        username,
        password
    });
    return res.data;
}

export async function register(dto: any) {
    const res = await api.post<any>('auth/register', dto);
    return res.data;
}

export async function getFriends() {
    const res = await api.get<GetFriendsResponse>(`users/connections/friends`);
    return res.data;
}

export async function getUser(id: string) {
    const res = await api.get<GetUserResponse>(`users/${id}`);
    return res.data;
}

export async function getConnections() {
    const res = await api.get<GetConnectionsResponse>(`users/connections`);
    return res.data;
}

export async function connect(id: string) {
    const res = await api.post<ConnectResponse>(`users/connections/${id}`);
    return res.data;
}

export async function removeFriend(id: string) {
    const res = await api.delete<DeleteFriendResponse>(`users/connections/${id}`);
    return res.data;
}

export async function acceptFriendRequest(request: AcceptFriendRequest) {
    const res = await api.post<AcceptFriendResponse>(`users/connections/accept`, request);
    return res.data;
}

export async function declineFriendRequest(request: DeclineFriendRequest) {
    const res = await api.post<DeclineFriendResponse>(`users/connections/decline`, request);
    return res.data;
}

export async function getInitialSnapshot(request: BoundingBox) {
    const res = await api.get<MapSnapshotResponse>(
        `maps/snapshot?minLon=${request.minLon}&maxLon=${request.maxLon}&minLat=${request.minLat}&maxLat=${request.maxLat}`);
    return res.data;
}

export async function sendBatchedLocations(request: SendBatchedLocationsRequest) {
    const res = await api.post(`locations/`, request);
    return res.data;
}

export async function createGeofence(request: Geofence) {
    const res = await api.post(`geofence/`, request);
    return res.data;
}

export async function getLocationHistory(range: TimeRangeOptions) {
    console.log(range.start.toISOString(), range.end.toISOString());
    const res = await api.get<LocationPoint[]>(
        `history/between?start=${range.start.toISOString()}&end=${range.end.toISOString()}&direction=${range.direction}`)
    return res.data;
}

export async function getMostVisitedLocations() {
    const res = await api.get<AnalyticsLocationPoint[]>(
        `history/popular`)
    return res.data;
}