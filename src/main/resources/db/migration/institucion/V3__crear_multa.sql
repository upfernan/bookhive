CREATE TABLE multa (
    id                UUID          NOT NULL,
    prestamo_id       UUID          NOT NULL,
    usuario_id        UUID          NOT NULL,
    fecha_generacion  DATE          NOT NULL,
    dias_retraso      INTEGER       NOT NULL,
    tarifa_diaria     NUMERIC(12,2) NOT NULL,
    valor_total       NUMERIC(12,2) NOT NULL,
    estado            VARCHAR(20)   NOT NULL,

    CONSTRAINT pk_multa              PRIMARY KEY (id),
    CONSTRAINT fk_multa_prestamo     FOREIGN KEY (prestamo_id) REFERENCES prestamo (id),
    CONSTRAINT ck_multa_estado       CHECK (estado IN ('PENDIENTE', 'PAGADA', 'CONDONADA')),
    CONSTRAINT ck_multa_dias         CHECK (dias_retraso > 0),
    CONSTRAINT ck_multa_valores      CHECK (tarifa_diaria >= 0 AND valor_total >= 0)
);

-- Para preguntar rapido si un usuario tiene multas sin pagar, que es lo que
-- consulta el validador antes de autorizar un prestamo.
CREATE INDEX ix_multa_usuario_estado
    ON multa (usuario_id, estado);

-- La tarifa se guarda en la multa, no se lee de la configuracion al consultarla:
-- si la institucion sube su tarifa manana, las multas viejas conservan la que
-- regia el dia que se generaron.
COMMENT ON COLUMN multa.tarifa_diaria IS
    'Tarifa vigente el dia en que se genero la multa; se congela a proposito';
