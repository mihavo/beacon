export type BoundingBox = {
    minLon: number;
    maxLon: number;
    minLat: number;
    maxLat: number;
}

export type Coords = { latitude: number; longitude: number };

export type MapSnapshotResponse = {
    timestamp: string
    userId: string,
    coords: Coords,
}[];

export type SendBatchedLocationsRequest = {
    coords: Coords;
    capturedAt: string;
}[]