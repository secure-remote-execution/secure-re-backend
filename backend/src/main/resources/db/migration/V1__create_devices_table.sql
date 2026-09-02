CREATE TABLE devices (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(20)  NOT NULL,
    ip_address          VARCHAR(45)  NOT NULL,
    vendor              VARCHAR(100) NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    last_config_change  TIMESTAMP    NOT NULL,
    managed_by          VARCHAR(150) NOT NULL
);

CREATE INDEX idx_devices_type ON devices (type);
