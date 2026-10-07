-- =========================================================================
-- ÁREA 06: RECURSOS HUMANOS (CU-26 a CU-30) - Sergio Alejandro Ramírez
-- =========================================================================

CREATE TABLE IF NOT EXISTS empleados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    cargo VARCHAR(50) NOT NULL,
    departamento VARCHAR(50) NOT NULL,
    salario_base DECIMAL(12,2) NOT NULL,
    fecha_ingreso DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id BIGINT UNIQUE,
    CONSTRAINT fk_emp_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS turnos_empleado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    sede VARCHAR(50) NOT NULL,
    CONSTRAINT fk_turno_emp FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

CREATE TABLE IF NOT EXISTS comisiones_ventas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id BIGINT NOT NULL,
    periodo VARCHAR(7) NOT NULL, -- YYYY-MM
    monto_ventas DECIMAL(15,2) NOT NULL,
    porcentaje_comision DECIMAL(5,2) NOT NULL,
    total_comision DECIMAL(12,2) NOT NULL,
    pagado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_comision_emp FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

-- Semilla
MERGE INTO empleados (id, documento, nombres, apellidos, cargo, departamento, salario_base, fecha_ingreso, activo, usuario_id)
KEY(documento)
VALUES
    (1, '80123456', 'Julián', 'Castro Rodríguez', 'Asesor Comercial Senior', 'Ventas y Finanzas', 3500000.00, '2023-01-15', TRUE, 2),
    (2, '52987654', 'Andrea', 'López Vargas', 'Analista Financiero', 'Riesgo y Finanzas', 4200000.00, '2022-06-01', TRUE, 3);
