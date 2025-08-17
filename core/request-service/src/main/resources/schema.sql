CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created TIMESTAMP,
    event_id BIGINT,
    requester_id BIGINT,
    status varchar(25) NOT NULL,

    CONSTRAINT fk_event_id FOREIGN KEY (event_id),
    CONSTRAINT fk_requester_id FOREIGN KEY (requester_id)
);