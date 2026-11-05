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
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE', -- DISPONIBLE, RESERVADO, VENDIDO, ALQUILADO, MANTENIMIENTO, EN_VERIFICACION
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

-- =========================================================================
-- CU-10: Controlar existencias en tiempo real para ventas (No CRUD)
-- =========================================================================

-- Último estado observado en el piso de la bodega. Es la fuente contra la que
-- el CU-10 compara el estado registrado en el sistema (paso 4 del flujo básico).
CREATE TABLE IF NOT EXISTS conteos_fisicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id BIGINT NOT NULL,
    bodega_id BIGINT,
    estado_fisico VARCHAR(30) NOT NULL, -- estado observado físicamente, mismo vocabulario que vehiculos.estado
    presente BOOLEAN NOT NULL DEFAULT TRUE,
    responsable VARCHAR(80) NOT NULL,
    observaciones VARCHAR(255),
    fecha_conteo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conteo_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_conteo_bodega FOREIGN KEY (bodega_id) REFERENCES bodegas(id)
);

-- Traza de cada verificación solicitada por el asesor comercial, se confirme o no
-- la disponibilidad (RN-10-08).
CREATE TABLE IF NOT EXISTS verificaciones_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id BIGINT NOT NULL,
    resultado VARCHAR(40) NOT NULL, -- DISPONIBLE_CONFIRMADO, OPERACION_ACTIVA, ESTADO_NO_VENDIBLE, SIN_CONTEO_FISICO, DISCREPANCIA_DETECTADA, INCIDENCIA_PENDIENTE
    motivo VARCHAR(255),
    estado_sistema VARCHAR(30),
    estado_fisico VARCHAR(30),
    solicitado_por VARCHAR(80) NOT NULL,
    fecha_verificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    bloqueo_expira TIMESTAMP,
    CONSTRAINT fk_verif_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id)
);

-- Incidencias abiertas por discrepancia entre el físico y el sistema (flujo alterno 4A).
CREATE TABLE IF NOT EXISTS incidencias_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id BIGINT NOT NULL,
    estado_sistema VARCHAR(30) NOT NULL,
    estado_fisico VARCHAR(30) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA', -- ABIERTA, RESUELTA
    detectada_por VARCHAR(80) NOT NULL,
    fecha_deteccion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resuelta_por VARCHAR(80),
    observacion_resolucion VARCHAR(255),
    fecha_resolucion TIMESTAMP,
    CONSTRAINT fk_incidencia_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id)
);

-- Datos semilla
MERGE INTO bodegas (codigo, nombre, ciudad, capacidad_vehiculos, activo)
KEY(codigo)
VALUES 
    ('BOD-NORTE', 'Bodega Principal Norte', 'Bogotá', 100, TRUE),
    ('BOD-CENTRO', 'Patio Central', 'Bogotá', 60, TRUE);

MERGE INTO vehiculos (vin, placa, tipo, marca, modelo_anio, linea, color, precio_venta, estado, kilometraje, bodega_id)
KEY(vin)
VALUES
    ('93HBL4820P1029381', 'KLU382', 'AUTOMOVIL', 'Toyota', 2024, 'Corolla XEI Híbrido', 'Blanco Perlado', 115000000.00, 'DISPONIBLE', 0, (SELECT id FROM bodegas WHERE codigo = 'BOD-NORTE')),
    ('3N1AB7AP8KL294819', 'LMP941', 'CAMIONETA', 'Mazda', 2024, 'CX-30 Grand Touring', 'Rojo Diamante', 128000000.00, 'DISPONIBLE', 1200, (SELECT id FROM bodegas WHERE codigo = 'BOD-NORTE')),
    ('1FTEW1E48KFA19283', 'NKS817', 'MOTOCICLETA', 'Yamaha', 2025, 'MT-09 SP', 'Azul Icon', 58000000.00, 'DISPONIBLE', 0, (SELECT id FROM bodegas WHERE codigo = 'BOD-CENTRO'));

-- Conteo físico inicial de la flota semilla, para que el CU-10 tenga contra qué comparar.
-- El NOT EXISTS lo hace re-ejecutable, igual que los MERGE de arriba.
INSERT INTO conteos_fisicos (vehiculo_id, bodega_id, estado_fisico, presente, responsable, observaciones)
SELECT v.id, v.bodega_id, v.estado, TRUE, 'bodega', 'Conteo físico de apertura'
FROM vehiculos v
WHERE NOT EXISTS (SELECT 1 FROM conteos_fisicos c WHERE c.vehiculo_id = v.id);
