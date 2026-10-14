-- Esquema COMPARTIDO: el registro de qué instituciones existen.
-- Es el único lugar donde conviven datos de todas las universidades, y por eso
-- solo guarda el nombre y a qué esquema pertenece cada una. Ningún dato de
-- negocio vive aquí (ADR-011).

CREATE TABLE institucion (
    id              UUID         NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    esquema         VARCHAR(63)  NOT NULL,
    estado          VARCHAR(20)  NOT NULL,
    fecha_registro  DATE         NOT NULL,

    CONSTRAINT pk_institucion          PRIMARY KEY (id),
    CONSTRAINT uq_institucion_esquema  UNIQUE (esquema),
    CONSTRAINT ck_institucion_estado   CHECK (estado IN ('APROVISIONANDO', 'ACTIVA', 'CANCELADA'))
);

-- El estado no es decoración: el resolvedor solo atiende instituciones ACTIVA.
-- Si la creación del esquema falla a mitad de camino, la institución se queda
-- en APROVISIONANDO y nadie entra a una base a medio construir.
COMMENT ON COLUMN institucion.estado IS
    'APROVISIONANDO mientras se crea su esquema; ACTIVA cuando ya se puede usar; CANCELADA al terminar la membresia';

-- El nombre del esquema se limita a 63 caracteres porque es el maximo que
-- PostgreSQL admite para un identificador.
COMMENT ON COLUMN institucion.esquema IS
    'Nombre del esquema de PostgreSQL donde viven los datos de esta institucion';
