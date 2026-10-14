-- Plantilla por INSTITUCION: estas migraciones se aplican una vez sobre el
-- esquema de cada universidad. No llevan columna de institucion: el esquema
-- al que se conecta ya la define (ADR-011).

CREATE TABLE ejemplar (
    id             UUID        NOT NULL,
    libro_id       UUID        NOT NULL,
    sede_id        UUID        NOT NULL,
    codigo_barras  VARCHAR(50) NOT NULL,
    estado         VARCHAR(20) NOT NULL,
    prestable      BOOLEAN     NOT NULL,

    CONSTRAINT pk_ejemplar                PRIMARY KEY (id),
    CONSTRAINT uq_ejemplar_codigo_barras  UNIQUE (codigo_barras),
    CONSTRAINT ck_ejemplar_estado         CHECK (estado IN ('DISPONIBLE', 'PRESTADO', 'RESERVADO', 'BAJA'))
);

COMMENT ON COLUMN ejemplar.prestable IS
    'Falso cuando el ejemplar es de consulta en sala y no sale de la biblioteca';
