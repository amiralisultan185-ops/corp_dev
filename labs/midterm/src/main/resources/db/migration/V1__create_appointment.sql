CREATE TABLE appointment (
    id           uuid PRIMARY KEY,
    business_key text NOT NULL,
    status       text NOT NULL,
    title        text NOT NULL,
    created_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT appointment_business_key_unique UNIQUE (business_key),
    CONSTRAINT appointment_status_known
        CHECK (status IN ('REQUESTED', 'CONFIRMED', 'COMPLETED'))
);
