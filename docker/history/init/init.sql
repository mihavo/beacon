CREATE DATABASE history_db;

\connect history_db

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION timescaledb;

create table location_history (
    timestamp timestamp(6) with time zone not null,
    user_id uuid not null,
    latitude double precision not null,
    longitude double precision not null,
    location GEOGRAPHY(POINT, 4326),
    primary key (timestamp, user_id)
);

create index location_history_timestamp_idx
    on location_history(timestamp desc);

create index location_history_user_id_timestamp_idx
    on location_history(user_id asc, timestamp desc);


SELECT create_hypertable('location_history', 'timestamp', 'user_id', number_partitions => 4,
                         chunk_time_interval => INTERVAL '1 day');

