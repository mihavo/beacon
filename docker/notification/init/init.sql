\connect notification_db;

CREATE TABLE subscriptions (
    id uuid not null,
    token varchar(256) not null unique,
    user_id uuid not null unique,
    device_id uuid not null unique,
    primary key (id)
);

CREATE index subscriptions_user_idx on subscriptions(user_id);
CREATE INDEX subscriptions_device_idx ON subscriptions(device_id);