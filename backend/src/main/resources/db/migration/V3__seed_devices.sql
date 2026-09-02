-- Seed de dispositivos FICTICIOS para el Laboratorio 3 (FDSI - ECI).
-- Ningún dato corresponde a infraestructura real.
INSERT INTO devices (id, name, type, ip_address, vendor, status, last_config_change, managed_by) VALUES
    ('11111111-1111-1111-1111-111111111111', 'FW-LAB-01', 'FIREWALL', '10.10.1.1', 'Aruba-Sim',   'ONLINE',  '2026-08-01 10:00:00', 'equipo-redes-eci'),
    ('22222222-2222-2222-2222-222222222222', 'RTR-LAB-01', 'ROUTER',   '10.10.1.2', 'Cisco-Sim',   'ONLINE',  '2026-08-02 09:30:00', 'equipo-redes-eci'),
    ('33333333-3333-3333-3333-333333333333', 'SW-LAB-01',  'SWITCH',   '10.10.1.3', 'Cisco-Sim',   'ONLINE',  '2026-08-03 14:15:00', 'equipo-redes-eci'),
    ('44444444-4444-4444-4444-444444444444', 'FW-LAB-02', 'FIREWALL', '10.10.2.1', 'Aruba-Sim',   'OFFLINE', '2026-07-28 08:00:00', 'equipo-redes-eci'),
    ('55555555-5555-5555-5555-555555555555', 'RTR-LAB-02', 'ROUTER',   '10.10.2.2', 'Juniper-Sim', 'UNKNOWN', '2026-07-15 17:45:00', 'equipo-redes-eci');
