-- =========================================================================
-- SISTEMA CONCESIONARIO - PONTIFICIA UNIVERSIDAD JAVERIANA
-- SCRIPT GENERAL 00: AUTENTICACIÓN Y ROLES (RF-26, RF-27)
-- =========================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Un usuario por perfil del SRS (sección 5). La contraseña se guarda como SHA-256.
-- admin/admin123, asesor/asesor123, analista/credito123, servicio/servicio123, bodega/bodega123, legal/legal123
MERGE INTO usuarios (id, username, password_hash, nombre_completo, rol, activo)
KEY(id)
VALUES
    (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrador General', 'ADMINISTRADOR', TRUE),
    (2, 'asesor', '2ac668df8973cd2b3c6e4dc7816eab15c3bba28dd98766d37860639be3816df9', 'Asesor Comercial', 'ASESOR_COMERCIAL', TRUE),
    (3, 'analista', '15ebc1ca24b4f0bd506d2e5303b4206fbb1d7486a6357335118afc369ed4f553', 'Analista de Crédito', 'ANALISTA_CREDITO', TRUE),
    (4, 'servicio', 'af5ca2a1077a3da48ad70befb2577b234a41ac2a62a7df5547fa653b50404cbd', 'Analista de Servicio al Cliente', 'ANALISTA_SERVICIO_CLIENTE', TRUE),
    (5, 'bodega', '3e2388e8ceddc313076daab3e4eb98a3feb2c0da2464e9c632eff130483208eb', 'Administrador de Bodega', 'ADMINISTRADOR_BODEGA', TRUE),
    (6, 'legal', 'bce14beaddab2ebc3e8e214509404b8d3d2b43b3968bf214ececb8a746cbe4f4', 'Analista de Documentación Legal', 'ANALISTA_DOCUMENTACION_LEGAL', TRUE);
