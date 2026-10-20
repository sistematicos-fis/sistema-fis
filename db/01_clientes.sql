-- =========================================================================
-- ÁREA 01: CLIENTES Y PQRS (CU-01 a CU-05) - Andrés Alquerque
-- =========================================================================

CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    tipo_documento VARCHAR(10) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    ingresos_mensuales DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    categoria_comercial VARCHAR(30) DEFAULT 'ESTANDAR',
    asesor_id BIGINT,
    indice_satisfaccion DECIMAL(5,2),
    nivel_satisfaccion VARCHAR(20),
    fecha_indice_satisfaccion TIMESTAMP,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Para bases locales creadas antes del CU-05
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS asesor_id BIGINT;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS indice_satisfaccion DECIMAL(5,2);
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS nivel_satisfaccion VARCHAR(20);
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS fecha_indice_satisfaccion TIMESTAMP;

CREATE TABLE IF NOT EXISTS pqrs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- PETICION, QUEJA, RECLAMO, SUGERENCIA
    descripcion VARCHAR(500) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    fecha_radicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP,
    CONSTRAINT fk_pqrs_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

ALTER TABLE pqrs ADD COLUMN IF NOT EXISTS fecha_resolucion TIMESTAMP;

CREATE TABLE IF NOT EXISTS encuestas_satisfaccion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    puntuacion_atencion INT NOT NULL, -- 1 a 5
    puntuacion_producto INT NOT NULL, -- 1 a 5
    puntuacion_tiempos INT NOT NULL,  -- 1 a 5
    comentarios VARCHAR(300),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_encuesta_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS alertas_satisfaccion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    asesor_id BIGINT,
    indice DECIMAL(5,2) NOT NULL,
    mensaje VARCHAR(300) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atendida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_alerta_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- Datos semilla para pruebas
MERGE INTO clientes (id, documento, tipo_documento, nombres, apellidos, email, telefono, ingresos_mensuales, categoria_comercial, asesor_id)
KEY(documento)
VALUES
    (1, '1018456789', 'CC', 'Carlos', 'Gómez Pérez', 'carlos.gomez@mail.com', '3101234567', 6500000.00, 'PREFERENCIAL', 2),
    (2, '1020987654', 'CC', 'María', 'Fernanda Ruiz', 'mf.ruiz@mail.com', '3209876543', 4200000.00, 'ESTANDAR', 2),
    (3, '900123456', 'NIT', 'Transportes del Norte SAS', 'Empresa', 'contacto@transnorte.com', '6017894561', 25000000.00, 'CORPORATIVO', 2);

-- Datos semilla CU-05

INSERT INTO encuestas_satisfaccion (cliente_id, puntuacion_atencion, puntuacion_producto, puntuacion_tiempos, comentarios, fecha)
SELECT c.id, 5, 5, 4, 'Semilla CU-05: excelente atención en la entrega', DATEADD(DAY, -15, CURRENT_TIMESTAMP)
FROM clientes c
WHERE c.documento = '1018456789'
  AND NOT EXISTS (SELECT 1 FROM encuestas_satisfaccion e WHERE e.comentarios = 'Semilla CU-05: excelente atención en la entrega');

INSERT INTO encuestas_satisfaccion (cliente_id, puntuacion_atencion, puntuacion_producto, puntuacion_tiempos, comentarios, fecha)
SELECT c.id, 5, 4, 5, 'Semilla CU-05: muy buen servicio posventa', DATEADD(DAY, -25, CURRENT_TIMESTAMP)
FROM clientes c
WHERE c.documento = '1018456789'
  AND NOT EXISTS (SELECT 1 FROM encuestas_satisfaccion e WHERE e.comentarios = 'Semilla CU-05: muy buen servicio posventa');

INSERT INTO encuestas_satisfaccion (cliente_id, puntuacion_atencion, puntuacion_producto, puntuacion_tiempos, comentarios, fecha)
SELECT c.id, 2, 3, 1, 'Semilla CU-05: la entrega del vehículo se demoró mucho', DATEADD(DAY, -20, CURRENT_TIMESTAMP)
FROM clientes c
WHERE c.documento = '1020987654'
  AND NOT EXISTS (SELECT 1 FROM encuestas_satisfaccion e WHERE e.comentarios = 'Semilla CU-05: la entrega del vehículo se demoró mucho');

INSERT INTO pqrs (cliente_id, tipo, descripcion, estado, fecha_radicacion, fecha_resolucion)
SELECT c.id, 'RECLAMO', 'Semilla CU-05: cobro de accesorios no solicitados', 'CERRADO',
       DATEADD(DAY, -50, CURRENT_TIMESTAMP), DATEADD(DAY, -32, CURRENT_TIMESTAMP)
FROM clientes c
WHERE c.documento = '1020987654'
  AND NOT EXISTS (SELECT 1 FROM pqrs p WHERE p.descripcion = 'Semilla CU-05: cobro de accesorios no solicitados');

INSERT INTO pqrs (cliente_id, tipo, descripcion, estado, fecha_radicacion, fecha_resolucion)
SELECT c.id, 'QUEJA', 'Semilla CU-05: mala atención en el taller', 'CERRADO',
       DATEADD(DAY, -35, CURRENT_TIMESTAMP), DATEADD(DAY, -23, CURRENT_TIMESTAMP)
FROM clientes c
WHERE c.documento = '1020987654'
  AND NOT EXISTS (SELECT 1 FROM pqrs p WHERE p.descripcion = 'Semilla CU-05: mala atención en el taller');

INSERT INTO pqrs (cliente_id, tipo, descripcion, estado, fecha_radicacion, fecha_resolucion)
SELECT c.id, 'QUEJA', 'Semilla CU-05: retraso en la matrícula de la flota', 'CERRADO',
       DATEADD(DAY, -45, CURRENT_TIMESTAMP), DATEADD(DAY, -31, CURRENT_TIMESTAMP)
FROM clientes c
WHERE c.documento = '900123456'
  AND NOT EXISTS (SELECT 1 FROM pqrs p WHERE p.descripcion = 'Semilla CU-05: retraso en la matrícula de la flota');

INSERT INTO pqrs (cliente_id, tipo, descripcion, estado, fecha_radicacion, fecha_resolucion)
SELECT c.id, 'PETICION', 'Semilla CU-05: solicitud de certificado de compra', 'CERRADO',
       DATEADD(DAY, -40, CURRENT_TIMESTAMP), DATEADD(DAY, -18, CURRENT_TIMESTAMP)
FROM clientes c
WHERE c.documento = '900123456'
  AND NOT EXISTS (SELECT 1 FROM pqrs p WHERE p.descripcion = 'Semilla CU-05: solicitud de certificado de compra');
