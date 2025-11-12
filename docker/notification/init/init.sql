\connect notification_db;

CREATE TABLE fcm_tokens (
    id uuid not null,
    token varchar(256) not null,
    user_id uuid not null,
    device_id uuid not null unique,
    primary key (id)
);

CREATE index fcm_tokens_user_idx on fcm_tokens(user_id);
CREATE INDEX fcm_tokens_device_idx ON fcm_tokens(device_id);