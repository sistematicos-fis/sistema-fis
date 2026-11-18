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

-- CU-21: matrícula (licencia de tránsito) e impuesto vehicular de cada vehículo
CREATE TABLE IF NOT EXISTS matriculas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id BIGINT NOT NULL UNIQUE,
    placa VARCHAR(10) NOT NULL UNIQUE,
    numero_licencia_transito VARCHAR(20) NOT NULL UNIQUE,
    organismo_transito VARCHAR(80) NOT NULL,
    fecha_matricula DATE NOT NULL,
    cilindraje_cc INT NOT NULL,
    estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVA', -- EN_TRAMITE, ACTIVA, SUSPENDIDA, CANCELADA
    ultimo_anio_impuesto_pagado INT,
    valor_impuesto_anual DECIMAL(12,2),
    CONSTRAINT fk_matricula_veh FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id)
);

-- Matrícula de los vehículos semilla del inventario (solo si todavía no tienen)
INSERT INTO matriculas (vehiculo_id, placa, numero_licencia_transito, organismo_transito, fecha_matricula,
                        cilindraje_cc, estado_registro, ultimo_anio_impuesto_pagado, valor_impuesto_anual)
SELECT v.id, v.placa, 'LT-' || v.placa, 'Secretaría Distrital de Movilidad de Bogotá',
       CASE v.vin WHEN '1FTEW1E48KFA19283' THEN DATE '2025-03-10' ELSE DATE '2024-02-15' END,
       CASE v.vin WHEN '93HBL4820P1029381' THEN 1798 WHEN '3N1AB7AP8KL294819' THEN 2488 ELSE 890 END,
       'ACTIVA',
       CASE v.vin WHEN '3N1AB7AP8KL294819' THEN 2024 ELSE YEAR(CURRENT_DATE) END,
       CASE v.vin WHEN '93HBL4820P1029381' THEN 1725000 WHEN '3N1AB7AP8KL294819' THEN 1920000 ELSE 870000 END
FROM vehiculos v
WHERE v.vin IN ('93HBL4820P1029381', '3N1AB7AP8KL294819', '1FTEW1E48KFA19283')
  AND v.placa IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM matriculas m WHERE m.vehiculo_id = v.id);


-- CU-23: pólizas multirriesgo de la flota de alquiler
-- El tomador de estas pólizas es el concesionario, no un cliente, y se registran a partir
-- de la póliza física: el cliente y el código del simulador dejan de ser obligatorios.
ALTER TABLE polizas_seguro ALTER COLUMN cliente_id SET NULL;
ALTER TABLE polizas_seguro ALTER COLUMN codigo_aprobacion_simulada SET NULL;


-- CU-24: SOAT de los vehículos y estado documental
CREATE TABLE IF NOT EXISTS soats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_poliza VARCHAR(40) NOT NULL UNIQUE,
    vehiculo_id BIGINT NOT NULL,
    aseguradora_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    valor_pagado DECIMAL(12,2) NOT NULL,
    codigo_autorizacion VARCHAR(40),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_soat_veh FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_soat_aseg FOREIGN KEY (aseguradora_id) REFERENCES aseguradoras(id)
);

-- Estado documental de cada vehículo (paso 7 y flujo 6A del CU-24)
CREATE TABLE IF NOT EXISTS documentacion_vehiculos (
    vehiculo_id BIGINT PRIMARY KEY,
    estado_documental VARCHAR(20) NOT NULL, -- AL_DIA, PENDIENTE
    observacion VARCHAR(255),
    fecha_actualizacion TIMESTAMP NOT NULL,
    CONSTRAINT fk_docveh_veh FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id)
);

-- SOAT semilla para probar los flujos del CU-24 (fechas relativas al primer arranque):
-- KLU382 vence en 10 días (renovación exitosa), LMP941 vence en 5 días (el simulador le reporta
-- un siniestro pendiente, flujo 4A) y NKS817 venció hace 15 días (Allianz rechaza motos, flujo 6A).
INSERT INTO soats (numero_poliza, vehiculo_id, aseguradora_id, fecha_inicio, fecha_vencimiento, valor_pagado, codigo_autorizacion)
SELECT 'SOAT-SEMILLA-' || v.placa, v.id,
       (SELECT id FROM aseguradoras WHERE codigo = 'SURA'),
       DATEADD(DAY, 1, DATEADD(YEAR, -1, DATEADD(DAY, CASE v.placa WHEN 'KLU382' THEN 10 WHEN 'LMP941' THEN 5 ELSE -15 END, CURRENT_DATE))),
       DATEADD(DAY, CASE v.placa WHEN 'KLU382' THEN 10 WHEN 'LMP941' THEN 5 ELSE -15 END, CURRENT_DATE),
       CASE v.tipo WHEN 'MOTOCICLETA' THEN 790000 WHEN 'CAMIONETA' THEN 980000 ELSE 570000 END,
       'AUT-SEMILLA'
FROM vehiculos v
WHERE v.placa IN ('KLU382', 'LMP941', 'NKS817')
  AND NOT EXISTS (SELECT 1 FROM soats s WHERE s.numero_poliza = 'SOAT-SEMILLA-' || v.placa);
