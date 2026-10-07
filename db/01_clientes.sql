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
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pqrs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- PETICION, QUEJA, RECLAMO, SUGERENCIA
    descripcion VARCHAR(500) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    fecha_radicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pqrs_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

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

-- Datos semilla para pruebas
MERGE INTO clientes (id, documento, tipo_documento, nombres, apellidos, email, telefono, ingresos_mensuales, categoria_comercial)
KEY(documento)
VALUES
    (1, '1018456789', 'CC', 'Carlos', 'Gómez Pérez', 'carlos.gomez@mail.com', '3101234567', 6500000.00, 'PREFERENCIAL'),
    (2, '1020987654', 'CC', 'María', 'Fernanda Ruiz', 'mf.ruiz@mail.com', '3209876543', 4200000.00, 'ESTANDAR'),
    (3, '900123456', 'NIT', 'Transportes del Norte SAS', 'Empresa', 'contacto@transnorte.com', '6017894561', 25000000.00, 'CORPORATIVO');
