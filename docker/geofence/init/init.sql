\connect geofence_db

CREATE EXTENSION IF NOT EXISTS postgis;

create table geofences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    target_id UUID NOT NULL,
    center GEOMETRY(POINT, 4326) NOT NULL,
    radius_meters DOUBLE PRECISION NOT NULL,
    trigger_type VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX geofences_center_idx ON geofences USING GIST(center);
