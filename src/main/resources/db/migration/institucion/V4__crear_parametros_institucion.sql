-- Los parametros que cada institucion ajusta por su cuenta, sin esperar una
-- version nueva del sistema (RN-10). Viven en el esquema de la institucion,
-- asi que cada universidad tiene los suyos.

CREATE TABLE parametros_institucion (
    id                            UUID    NOT NULL,
    dias_prestamo                 INTEGER NOT NULL,
    maximo_prestamos_simultaneos  INTEGER NOT NULL,

    CONSTRAINT pk_parametros_institucion  PRIMARY KEY (id),
    CONSTRAINT ck_parametros_dias         CHECK (dias_prestamo BETWEEN 1 AND 90),
    CONSTRAINT ck_parametros_maximo       CHECK (maximo_prestamos_simultaneos BETWEEN 1 AND 20)
);

-- Una sola fila por institucion: el indice sobre una constante impide la segunda.
CREATE UNIQUE INDEX uq_parametros_institucion_fila_unica
    ON parametros_institucion ((TRUE));

-- Valores de arranque. Cada institucion los cambia desde su panel de
-- administracion; estos son solo para que el sistema funcione el primer dia.
INSERT INTO parametros_institucion (id, dias_prestamo, maximo_prestamos_simultaneos)
VALUES ('11111111-1111-1111-1111-111111111111', 15, 3);
