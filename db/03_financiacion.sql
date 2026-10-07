-- =========================================================================
-- ÁREA 03: FINANCIACIÓN (CU-11 a CU-15) - Jerónimo Martin
-- =========================================================================

-- CU-11: Entidades Financieras Aliadas
CREATE TABLE IF NOT EXISTS entidades_financieras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    tasa_base_ea DECIMAL(6,4) NOT NULL, -- Efectiva Anual (ej: 0.1650 = 16.50%)
    scoring_minimo INT NOT NULL DEFAULT 650,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CU-12: Tipos / Planes de Crédito
CREATE TABLE IF NOT EXISTS tipos_credito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    entidad_financiera_id BIGINT NOT NULL,
    tasa_interes_ea DECIMAL(6,4) NOT NULL,
    plazo_minimo_meses INT NOT NULL,
    plazo_maximo_meses INT NOT NULL,
    porcentaje_financiacion_max DECIMAL(5,2) NOT NULL, -- ej: 80.00%
    requiere_garantia_prenda BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_tipo_entidad FOREIGN KEY (entidad_financiera_id) REFERENCES entidades_financieras(id)
);

-- CU-14: Solicitudes de Crédito
CREATE TABLE IF NOT EXISTS solicitudes_credito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_radicado VARCHAR(30) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    vehiculo_id BIGINT NOT NULL,
    tipo_credito_id BIGINT NOT NULL,
    monto_solicitado DECIMAL(15,2) NOT NULL,
    plazo_meses INT NOT NULL,
    cuota_inicial DECIMAL(15,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'RADICADA', -- RADICADA, EN_EVALUACION, APROBADA, RECHAZADA, FORMALIZADA
    ingresos_certificados DECIMAL(15,2) NOT NULL,
    gastos_declarados DECIMAL(15,2) NOT NULL,
    cuota_estimada DECIMAL(15,2) NOT NULL,
    porcentaje_endeudamiento DECIMAL(5,2) NOT NULL,
    score_crediticio_simulado INT,
    observaciones VARCHAR(500),
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sol_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_sol_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_sol_tipo FOREIGN KEY (tipo_credito_id) REFERENCES tipos_credito(id)
);

-- CU-15: Créditos Formalizados (Aprobación Externa Simulada)
CREATE TABLE IF NOT EXISTS creditos_formalizados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id BIGINT NOT NULL UNIQUE,
    codigo_aprobacion_banco VARCHAR(50) NOT NULL UNIQUE,
    entidad_id BIGINT NOT NULL,
    monto_aprobado DECIMAL(15,2) NOT NULL,
    tasa_mensual_aplicada DECIMAL(8,6) NOT NULL,
    plazo_meses INT NOT NULL,
    fecha_formalizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, FINALIZADO, CANCELADO, MORA
    saldo_capital_pendiente DECIMAL(15,2) NOT NULL,
    CONSTRAINT fk_credito_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes_credito(id),
    CONSTRAINT fk_credito_entidad FOREIGN KEY (entidad_id) REFERENCES entidades_financieras(id)
);

-- CU-13: Plan de Cuotas de Amortización (Regla no trivial: Francés con centavos)
CREATE TABLE IF NOT EXISTS cuotas_amortizacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credito_formalizado_id BIGINT NOT NULL,
    numero_cuota INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cuota_fija DECIMAL(15,2) NOT NULL,
    abono_capital DECIMAL(15,2) NOT NULL,
    interes DECIMAL(15,2) NOT NULL,
    saldo_restante DECIMAL(15,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, PAGADA, MORA
    CONSTRAINT fk_cuota_credito FOREIGN KEY (credito_formalizado_id) REFERENCES creditos_formalizados(id),
    CONSTRAINT uq_credito_cuota UNIQUE (credito_formalizado_id, numero_cuota)
);

-- Pagos de Cuotas de Financiación
CREATE TABLE IF NOT EXISTS pagos_credito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credito_formalizado_id BIGINT NOT NULL,
    numero_cuota INT NOT NULL,
    monto_pagado DECIMAL(15,2) NOT NULL,
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    numero_recibo VARCHAR(40) NOT NULL UNIQUE,
    metodo_pago VARCHAR(30) NOT NULL, -- TRANSFERENCIA_SIMULADA, CHEQUE_GERENCIA, EFECTIVO
    CONSTRAINT fk_pago_credito FOREIGN KEY (credito_formalizado_id) REFERENCES creditos_formalizados(id)
);

-- Índices de consulta de Financiación
CREATE INDEX IF NOT EXISTS idx_solicitud_cliente ON solicitudes_credito(cliente_id);
CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON solicitudes_credito(estado);
CREATE INDEX IF NOT EXISTS idx_cuota_credito ON cuotas_amortizacion(credito_formalizado_id);

-- Datos Semilla de Financiación
MERGE INTO entidades_financieras (id, codigo, nombre, nit, tasa_base_ea, scoring_minimo, activo)
KEY(codigo)
VALUES 
    (1, 'BANCOL', 'Bancolombia S.A.', '890.903.938-8', 0.1650, 680, TRUE),
    (2, 'DAVIV', 'Banco Davivienda S.A.', '860.034.313-7', 0.1580, 650, TRUE),
    (3, 'OCCID', 'Banco de Occidente (AutoOccidente)', '890.300.279-4', 0.1720, 620, TRUE);

MERGE INTO tipos_credito (id, codigo, nombre, descripcion, entidad_financiera_id, tasa_interes_ea, plazo_minimo_meses, plazo_maximo_meses, porcentaje_financiacion_max, requiere_garantia_prenda, activo)
KEY(codigo)
VALUES
    (1, 'AUTO-TRAD-BC', 'Crédito Vehículo Tradicional Particular', 'Financiación hasta 84 meses para vehículos particulares', 1, 0.1650, 12, 84, 80.00, TRUE, TRUE),
    (2, 'AUTO-ECO-DV', 'Eco-Crédito Vehículos Híbridos y Eléctricos', 'Tasa preferencial para autos de bajas emisiones', 2, 0.1420, 12, 72, 90.00, TRUE, TRUE),
    (3, 'MOTO-OCC', 'Financiación Motocicletas de Alto Cilindro', 'Crédito especializado para motocicletas deportivas y touring', 3, 0.1980, 6, 48, 70.00, TRUE, TRUE);
