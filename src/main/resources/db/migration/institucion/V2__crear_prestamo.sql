CREATE TABLE prestamo (
    id                 UUID        NOT NULL,
    ejemplar_id        UUID        NOT NULL,
    usuario_id         UUID        NOT NULL,
    bibliotecario_id   UUID        NOT NULL,
    fecha_prestamo     DATE        NOT NULL,
    fecha_vencimiento  DATE        NOT NULL,
    estado             VARCHAR(20) NOT NULL,

    CONSTRAINT pk_prestamo            PRIMARY KEY (id),
    CONSTRAINT fk_prestamo_ejemplar   FOREIGN KEY (ejemplar_id) REFERENCES ejemplar (id),
    CONSTRAINT ck_prestamo_estado     CHECK (estado IN ('ACTIVO', 'VENCIDO', 'DEVUELTO')),
    CONSTRAINT ck_prestamo_vigencia   CHECK (fecha_vencimiento > fecha_prestamo)
);

-- UN EJEMPLAR, UN PRESTAMO ACTIVO. Esta es la regla que impide que dos personas
-- se lleven el mismo libro, y la impone el motor, no el codigo: aunque dos
-- peticiones lleguen en el mismo instante, PostgreSQL deja pasar una sola.
-- Es lo que el DAS llama "impedir condiciones de carrera sobre un mismo ejemplar".
CREATE UNIQUE INDEX uq_prestamo_ejemplar_activo
    ON prestamo (ejemplar_id)
    WHERE estado = 'ACTIVO';

-- Para contar cuantos prestamos activos tiene un usuario sin recorrer la tabla.
CREATE INDEX ix_prestamo_usuario_estado
    ON prestamo (usuario_id, estado);

-- Para la tarea que marca como vencidos los prestamos del dia.
CREATE INDEX ix_prestamo_vencimiento
    ON prestamo (fecha_vencimiento)
    WHERE estado = 'ACTIVO';
