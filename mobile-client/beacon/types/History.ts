export interface TimeRangeOptions {
    start: Date,
    end: Date,
    direction: string,
}

export interface LocationPoint {
    latitude: number;
    longitude: number;
    timestamp: string;
    address?: string;
}