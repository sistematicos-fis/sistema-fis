-- =========================================================================
-- ÁREA 07: DOCUMENTACIÓN LEGAL (CU-21 a CU-25) - Samuel Montaña
-- =========================================================================

CREATE TABLE IF NOT EXISTS aseguradoras (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    telefono_contacto VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE
    );

-- CU-22: coberturas que cubre el convenio (SOAT y/o responsabilidad civil por daños materiales a terceros)
ALTER TABLE aseguradoras ADD COLUMN IF NOT EXISTS cubre_soat BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE aseguradoras ADD COLUMN IF NOT EXISTS cubre_responsabilidad_civil BOOLEAN NOT NULL DEFAULT TRUE;

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
-- Sin id explícito para que el autoincremento no choque con los registros nuevos (CU-22)
MERGE INTO aseguradoras (codigo, nombre, nit, telefono_contacto, activo)
    KEY(codigo)
    VALUES
    ('SURA', 'Seguros Generales Suramericana S.A.', '890.903.407-9', '018000518888', TRUE),
    ('ALLIANZ', 'Allianz Seguros S.A.', '860.026.182-5', '018000513888', TRUE),
    ('BOLIVAR', 'Seguros Bolívar S.A.', '860.002.503-2', '018000123322', TRUE);

-- Bases locales creadas antes de este cambio ya tienen los ids 1 a 3: se reubica el contador
ALTER TABLE aseguradoras ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM aseguradoras);

-- CU-25: certificados de revisión técnico-mecánica (RTM) de los vehículos
CREATE TABLE IF NOT EXISTS revisiones_tecnico_mecanicas (
                                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                            vehiculo_id BIGINT NOT NULL,
                                                            numero_certificado VARCHAR(30) NOT NULL UNIQUE,
    centro_diagnostico VARCHAR(80) NOT NULL,
    fecha_expedicion DATE NOT NULL,
    fecha_vencimiento DATE,
    resultado VARCHAR(15) NOT NULL, -- APROBADA, RECHAZADA
    CONSTRAINT fk_rtm_veh FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id)
    );