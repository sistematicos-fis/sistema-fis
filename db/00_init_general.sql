-- =========================================================================
-- SISTEMA CONCESIONARIO - PONTIFICIA UNIVERSIDAD JAVERIANA
-- SCRIPT GENERAL 00: SEGURIDAD, AUTENTICACIÓN Y ROLES (RF-36, RF-37)
-- =========================================================================

-- Tabla de Usuarios del Sistema (Autenticación Obligatoria)
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserción de usuarios base por rol para pruebas del sistema
MERGE INTO usuarios (id, username, password_hash, nombre_completo, rol, activo)
KEY(username)
VALUES 
    (1, 'admin', 'admin123', 'Administrador General', 'ADMINISTRADOR', TRUE),
    (2, 'asesor', 'asesor123', 'Asesor Comercial Financiero', 'ASESOR_COMERCIAL', TRUE),
    (3, 'analista', 'analista123', 'Analista de Riesgo Crediticio', 'ANALISTA_CREDITO', TRUE),
    (4, 'cajero', 'cajero123', 'Cajero de Tesorería', 'CAJERO', TRUE),
    (5, 'gerente', 'gerente123', 'Gerente de Sucursal', 'GERENTE', TRUE);
