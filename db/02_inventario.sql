-- =========================================================================
-- ÁREA 02: INVENTARIO Y BODEGA (CU-06 a CU-10) - Juan Pablo León
-- =========================================================================

CREATE TABLE IF NOT EXISTS bodegas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    ciudad VARCHAR(50) NOT NULL,
    capacidad_vehiculos INT NOT NULL DEFAULT 50,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS vehiculos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin VARCHAR(30) NOT NULL UNIQUE,
    placa VARCHAR(10) UNIQUE,
    tipo VARCHAR(20) NOT NULL, -- AUTOMOVIL, CAMIONETA, MOTOCICLETA, UTILITARIO
    marca VARCHAR(50) NOT NULL,
    modelo_anio INT NOT NULL,
    linea VARCHAR(50) NOT NULL,
    color VARCHAR(30) NOT NULL,
    precio_venta DECIMAL(15,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE', -- DISPONIBLE, RESERVADO, VENDIDO, ALQUILADO, MANTENIMIENTO
    kilometraje INT NOT NULL DEFAULT 0,
    bodega_id BIGINT,
    CONSTRAINT fk_vehiculo_bodega FOREIGN KEY (bodega_id) REFERENCES bodegas(id)
);

CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL, -- ENTRADA_IMPORTACION, TRASLADO, SALIDA_VENTA, SALIDA_ALQUILER
    bodega_origen_id BIGINT,
    bodega_destino_id BIGINT,
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones VARCHAR(255),
    CONSTRAINT fk_mov_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id)
);

-- Datos semilla
MERGE INTO bodegas (id, codigo, nombre, ciudad, capacidad_vehiculos, activo)
KEY(codigo)
VALUES 
    (1, 'BOD-NORTE', 'Bodega Principal Norte', 'Bogotá', 100, TRUE),
    (2, 'BOD-CENTRO', 'Patio Central', 'Bogotá', 60, TRUE);

MERGE INTO vehiculos (id, vin, placa, tipo, marca, modelo_anio, linea, color, precio_venta, estado, kilometraje, bodega_id)
KEY(vin)
VALUES
    (1, '93HBL4820P1029381', 'KLU382', 'AUTOMOVIL', 'Toyota', 2024, 'Corolla XEI Híbrido', 'Blanco Perlado', 115000000.00, 'DISPONIBLE', 0, 1),
    (2, '3N1AB7AP8KL294819', 'LMP941', 'CAMIONETA', 'Mazda', 2024, 'CX-30 Grand Touring', 'Rojo Diamante', 128000000.00, 'DISPONIBLE', 1200, 1),
    (3, '1FTEW1E48KFA19283', 'NKS817', 'MOTOCICLETA', 'Yamaha', 2025, 'MT-09 SP', 'Azul Icon', 58000000.00, 'DISPONIBLE', 0, 2);
