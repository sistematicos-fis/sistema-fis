CREATE TABLE IF NOT EXISTS entidades_financieras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    tasa_base_ea DECIMAL(6,4) NOT NULL,
    scoring_minimo INT NOT NULL DEFAULT 650,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

MERGE INTO entidades_financieras (codigo, nombre, nit, tasa_base_ea, scoring_minimo, activo)
KEY(codigo)
VALUES 
    ('BANCOL', 'Bancolombia S.A.', '890.903.938-8', 0.1650, 680, TRUE),
    ('DAVIV', 'Banco Davivienda S.A.', '860.034.313-7', 0.1580, 650, TRUE),
    ('OCCID', 'Banco de Occidente (AutoOccidente)', '890.300.279-4', 0.1720, 620, TRUE);
