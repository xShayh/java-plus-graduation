CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created TIMESTAMP,
    event_id BIGINT,
    requester_id BIGINT,
    status varchar(25) NOT NULL
);