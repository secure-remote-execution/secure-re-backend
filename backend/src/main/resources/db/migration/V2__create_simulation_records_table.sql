CREATE TABLE simulation_records (
    id             UUID PRIMARY KEY,
    device_id      UUID NOT NULL REFERENCES devices (id),
    device_name    VARCHAR(100) NOT NULL,
    script_name    VARCHAR(150) NOT NULL,
    parameters     TEXT,
    success        BOOLEAN      NOT NULL,
    output         VARCHAR(500) NOT NULL,
    source_ip      VARCHAR(45),
    executed_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_simulation_records_device_id ON simulation_records (device_id);
CREATE INDEX idx_simulation_records_executed_at ON simulation_records (executed_at);
