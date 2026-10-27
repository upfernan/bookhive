CREATE TABLE dominio_institucion (
    dominio         VARCHAR(253)  NOT NULL,
    institucion_id  UUID          NOT NULL,
    creado_en       TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT pk_dominio_institucion              PRIMARY KEY (dominio),
    CONSTRAINT fk_dominio_institucion_institucion  FOREIGN KEY (institucion_id) REFERENCES institucion (id),
    CONSTRAINT ck_dominio_institucion_formato      CHECK (dominio = lower(dominio) AND dominio LIKE '%_._%')
);

CREATE INDEX ix_dominio_institucion_institucion ON dominio_institucion (institucion_id);

GRANT SELECT ON dominio_institucion TO ${appUser};

ALTER TABLE institucion ADD COLUMN alta VARCHAR(20) NOT NULL DEFAULT 'MANUAL';

ALTER TABLE institucion ADD CONSTRAINT ck_institucion_alta CHECK (alta IN ('MANUAL', 'AUTOREGISTRO'));

GRANT INSERT ON persona TO ${appUser};

INSERT INTO mensaje (codigo, texto) VALUES
    ('registration.data.invalid', 'Para registrarse indique su nombre (máximo 200 caracteres) y entre con su correo institucional.'),
    ('registration.email.not-verified', 'Primero confirme su correo con el enlace que le enviamos y vuelva a intentarlo.'),
    ('registration.domain.not-allowed', 'El dominio de su correo no pertenece a ninguna institución activa en BookHive.'),
    ('registration.already-registered', 'Esa cuenta ya está registrada en BookHive. Inicie sesión.'),
    ('data-policy.public.not-found', 'No encontramos el aviso de tratamiento de datos para ese correo. Revise que sea su correo institucional.'),
    ('registration.institution.unavailable', 'En este momento no podemos crear la biblioteca de su institución. Intente de nuevo en unos minutos.'),
    ('data-policy.default', 'La institución es la responsable del tratamiento de sus datos personales dentro de BookHive, conforme a la Ley 1581 de 2012 y el Decreto 1377 de 2013.

Datos que se tratan: su nombre, su correo institucional y el registro de sus préstamos, devoluciones, reservas, multas y pagos.

Para qué se usan: prestarle material de la biblioteca, llevar la fila de reservas, calcular y cobrar las multas por devolución tardía, y enviarle avisos sobre sus préstamos, reservas y pagos. No se venden ni se ceden a terceros. Los pagos en línea los procesa la pasarela de pagos, que recibe solo lo necesario para cobrar.

Sus derechos: conocer, actualizar y corregir sus datos, pedir prueba de esta autorización, saber cómo se han usado, revocar la autorización y pedir que se eliminen cuando no exista una obligación legal o contractual de conservarlos. Puede ejercerlos ante la biblioteca de su institución.

Al eliminar sus datos se borran su nombre y su correo; el registro de préstamos, multas y pagos se conserva sin identificarlo, porque sirve de soporte contable.

Al aceptar, autoriza a la institución a tratar sus datos para estos fines.');
