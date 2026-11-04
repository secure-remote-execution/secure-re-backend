CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username       VARCHAR(50) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    role           VARCHAR(20) NOT NULL,
    enabled        BOOLEAN NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Usuarios semilla para el laboratorio (Blue/Red Team). Passwords hasheadas con BCrypt.
-- admin / Admin123!  (rol ADMIN: puede ejecutar simulaciones de scripts)
-- viewer / Viewer123! (rol VIEWER: solo lectura del inventario e historial)
INSERT INTO users (username, password_hash, role) VALUES
('admin',  '$2a$10$VGkV2geNK/KNAhxL29uCMOUwtixXPWUV4RnZUho1S/armB7rpiqpa', 'ADMIN'),
('viewer', '$2a$10$f6GYOoI2m2pKEMzAigptROiatAPpFN7oH49Dm9uu3rwufAIXqdOM.', 'VIEWER');

-- Trazabilidad: cada simulación queda atada al usuario autenticado que la ejecutó,
-- en vez de quedar anónima (hallazgo de Repudiation del análisis STRIDE del Lab 3).
ALTER TABLE script_simulations ADD COLUMN performed_by VARCHAR(50) NOT NULL DEFAULT 'system';
