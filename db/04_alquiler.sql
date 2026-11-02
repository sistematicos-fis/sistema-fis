-- =========================================================================
-- ÁREA 04: ALQUILER (CU-16 a CU-20) - Samuel Borras
-- =========================================================================

-- CU-16: Puntos de Entrega y Devolución
CREATE TABLE IF NOT EXISTS puntos_entrega (
    id VARCHAR(30) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(150) NOT NULL,
    ciudad VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tarifas_alquiler (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_vehiculo VARCHAR(20) NOT NULL,
    tarifa_diaria DECIMAL(12,2) NOT NULL,
    deposito_garantia DECIMAL(12,2) NOT NULL,
    kilometraje_libre BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS contratos_alquiler (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_contrato VARCHAR(30) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    vehiculo_id BIGINT NOT NULL,
    punto_entrega_origen_id VARCHAR(30) NOT NULL,
    punto_entrega_destino_id VARCHAR(30) NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin_estimada TIMESTAMP NOT NULL,
    fecha_devolucion_real TIMESTAMP,
    valor_total DECIMAL(12,2) NOT NULL,
    deposito_pagado DECIMAL(12,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'RESERVADO', -- RESERVADO, EN_CURSO, FINALIZADO, CANCELADO
    CONSTRAINT fk_alquiler_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_alquiler_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_alquiler_origen FOREIGN KEY (punto_entrega_origen_id) REFERENCES puntos_entrega(id),
    CONSTRAINT fk_alquiler_destino FOREIGN KEY (punto_entrega_destino_id) REFERENCES puntos_entrega(id)
);

-- Semilla inicial
MERGE INTO puntos_entrega (id, nombre, direccion, ciudad, activo)
KEY(id)
VALUES
    ('SEDE-01', 'Sede Norte Autopista', 'Autopista Norte # 170-20', 'Bogotá', TRUE),
    ('SEDE-02', 'Sede Aeropuerto El Dorado', 'Avenida El Dorado # 103-09', 'Bogotá', TRUE),
    ('SEDE-03', 'Sede Centro Empresarial', 'Carrera 7 # 72-11', 'Bogotá', TRUE);
