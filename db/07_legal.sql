-- =========================================================================
-- ÁREA 07: DOCUMENTACIÓN LEGAL (CU-31 a CU-35) - Samuel Montaña
-- =========================================================================

CREATE TABLE IF NOT EXISTS aseguradoras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    telefono_contacto VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS polizas_seguro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_poliza VARCHAR(50) NOT NULL UNIQUE,
    aseguradora_id BIGINT NOT NULL,
    vehiculo_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    tipo_poliza VARCHAR(30) NOT NULL, -- SOAT, TODO_RIESGO_RESPONSABILIDAD, EXTRACONTRACTUAL
    valor_prima DECIMAL(12,2) NOT NULL,
    valor_cobertura DECIMAL(15,2) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'VIGENTE', -- VIGENTE, VENCIDA, CANCELADA
    codigo_aprobacion_simulada VARCHAR(50) NOT NULL,
    CONSTRAINT fk_poliza_aseg FOREIGN KEY (aseguradora_id) REFERENCES aseguradoras(id),
    CONSTRAINT fk_poliza_veh FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_poliza_cli FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS actas_traspaso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_radicado_runt VARCHAR(50) NOT NULL UNIQUE,
    vehiculo_id BIGINT NOT NULL,
    comprador_id BIGINT NOT NULL,
    fecha_tramite DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'RADICADO', -- RADICADO, EN_TRAMITE, APROBADO_RUNT, RECHAZADO
    organismo_transito VARCHAR(80) NOT NULL DEFAULT 'Secretaría de Movilidad Bogotá',
    CONSTRAINT fk_traspaso_veh FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_traspaso_cli FOREIGN KEY (comprador_id) REFERENCES clientes(id)
);

-- Semilla
MERGE INTO aseguradoras (id, codigo, nombre, nit, telefono_contacto, activo)
KEY(codigo)
VALUES
    (1, 'SURA', 'Seguros Generales Suramericana S.A.', '890.903.407-9', '018000518888', TRUE),
    (2, 'ALLIANZ', 'Allianz Seguros S.A.', '860.026.182-5', '018000513888', TRUE),
    (3, 'BOLIVAR', 'Seguros Bolívar S.A.', '860.002.503-2', '018000123322', TRUE);
