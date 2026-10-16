CREATE TABLE usuario (
    id          UUID         NOT NULL,
    nombre      VARCHAR(200) NOT NULL,
    correo      VARCHAR(254) NOT NULL,
    estado      VARCHAR(20)  NOT NULL,
    creado_en   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_usuario          PRIMARY KEY (id),
    CONSTRAINT uq_usuario_correo   UNIQUE (correo),
    CONSTRAINT ck_usuario_estado   CHECK (estado IN ('ACTIVO', 'SUSPENDIDO', 'RETIRADO'))
);

COMMENT ON TABLE usuario IS
    'La persona dentro de ESTA institucion. Su id es el mismo de compartido.persona: el nombre y el correo viven aca porque son datos de la institucion y no se mezclan entre universidades (ADR-011)';

COMMENT ON COLUMN usuario.estado IS
    'SUSPENDIDO y RETIRADO no se borran: un prestamo viejo debe poder explicar a quien se le hizo';

ALTER TABLE prestamo
    ADD CONSTRAINT fk_prestamo_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);

ALTER TABLE multa
    ADD CONSTRAINT fk_multa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);
