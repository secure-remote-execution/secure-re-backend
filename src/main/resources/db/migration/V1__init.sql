CREATE TABLE devices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(60)  NOT NULL,
    type                VARCHAR(20)  NOT NULL,
    ip_address          VARCHAR(45)  NOT NULL,
    vendor              VARCHAR(60)  NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    last_config_change  TIMESTAMPTZ,
    managed_by          VARCHAR(60)
);

CREATE TABLE script_simulations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id     UUID NOT NULL REFERENCES devices(id),
    script_name   VARCHAR(100) NOT NULL,
    parameters    VARCHAR(500),
    success       BOOLEAN NOT NULL,
    output        VARCHAR(300) NOT NULL,
    requested_at  TIMESTAMPTZ NOT NULL
);

-- Seed ficticio: inventario mínimo para el ejercicio Red Team / Blue Team.
-- Ningún dato aquí corresponde a infraestructura real.
INSERT INTO devices (name, type, ip_address, vendor, status, last_config_change, managed_by) VALUES
('FW-LAB-01',  'FIREWALL', '10.10.1.1',  'Aruba-Sim',  'ONLINE',  now() - interval '3 days',  'Equipo Blue Team'),
('FW-LAB-02',  'FIREWALL', '10.10.1.2',  'Cisco-Sim',  'OFFLINE', now() - interval '10 days', 'Equipo Blue Team'),
('RTR-LAB-01', 'ROUTER',   '10.10.2.1',  'Cisco-Sim',  'ONLINE',  now() - interval '1 days',  'Equipo Blue Team'),
('SW-LAB-01',  'SWITCH',   '10.10.3.1',  'Aruba-Sim',  'ONLINE',  now() - interval '5 days',  'Equipo Blue Team'),
('SW-LAB-02',  'SWITCH',   '10.10.3.2',  'Huawei-Sim', 'UNKNOWN', now() - interval '20 days', 'Equipo Blue Team');
