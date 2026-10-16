CREATE TABLE persona (
    id              UUID         NOT NULL,
    identidad       VARCHAR(255) NOT NULL,
    institucion_id  UUID         NOT NULL,
    estado          VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_persona             PRIMARY KEY (id),
    CONSTRAINT uq_persona_identidad   UNIQUE (identidad),
    CONSTRAINT fk_persona_institucion FOREIGN KEY (institucion_id) REFERENCES institucion (id),
    CONSTRAINT ck_persona_estado      CHECK (estado IN ('ACTIVA', 'INACTIVA'))
);

COMMENT ON TABLE persona IS
    'Relaciona a quien inicia sesion con su institucion. Vive en el esquema compartido porque hay que consultarla ANTES de saber a cual esquema conectarse (ADR-011, ADR-028)';

COMMENT ON COLUMN persona.identidad IS
    'El identificador que entrega el proveedor de identidad, lo unico que se le pide: no guarda el rol ni la institucion (ADR-028)';

COMMENT ON COLUMN persona.id IS
    'El identificador propio de BookHive. Es el que viaja al resto del sistema, por ejemplo a prestamo.usuario_id, para no regar el identificador del proveedor por todas las tablas';

CREATE INDEX ix_persona_institucion ON persona (institucion_id);
