export type GeofenceType = 'NEAR' | 'ENTER';

export interface Geofence {
    userId: string;
    type: GeofenceType;
    centerLatitude: number;
    centerLongitude: number;
    radiusMeters: number;
}