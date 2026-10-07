-- =========================================================================
-- ÁREA 05: MANTENIMIENTO Y FLOTA (CU-21 a CU-25) - Jaime Andrés Molina
-- =========================================================================

CREATE TABLE IF NOT EXISTS talleres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(120) NOT NULL,
    capacidad_bahias INT NOT NULL DEFAULT 8,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS ordenes_mantenimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_orden VARCHAR(30) NOT NULL UNIQUE,
    vehiculo_id BIGINT NOT NULL,
    taller_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- PREVENTIVO, CORRECTIVO, ALISTAMIENTO
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_salida_estimada TIMESTAMP,
    fecha_entrega_real TIMESTAMP,
    costo_total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_DIAGNOSTICO', -- EN_DIAGNOSTICO, EN_REPARACION, COMPLETADO, ENTREGADO
    diagnostico VARCHAR(500),
    CONSTRAINT fk_mant_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_mant_taller FOREIGN KEY (taller_id) REFERENCES talleres(id)
);

CREATE TABLE IF NOT EXISTS repuestos_mantenimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    orden_id BIGINT NOT NULL,
    codigo_repuesto VARCHAR(30) NOT NULL,
    descripcion VARCHAR(150) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_repuesto_orden FOREIGN KEY (orden_id) REFERENCES ordenes_mantenimiento(id)
);

-- Semilla
MERGE INTO talleres (id, codigo, nombre, direccion, capacidad_bahias, activo)
KEY(codigo)
VALUES
    (1, 'TAL-CENTRAL', 'Taller Central de Servicio', 'Calle 68 # 24-10', 12, TRUE),
    (2, 'TAL-NORTE', 'Taller Rápido Norte', 'Carrera 45 # 134-22', 6, TRUE);
