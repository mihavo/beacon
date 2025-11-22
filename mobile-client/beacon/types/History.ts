export interface TimeRangeOptions {
    start: Date,
    end: Date,
    direction: string,
}

export interface LocationPoint {
    id: string;
    latitude: number;
    longitude: number;
    timestamp: string;
    address?: string;
}