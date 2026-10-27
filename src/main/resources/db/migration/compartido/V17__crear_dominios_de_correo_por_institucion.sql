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

GRANT INSERT ON persona TO ${appUser};

INSERT INTO mensaje (codigo, texto) VALUES
    ('registration.data.invalid', 'Para registrarse indique su nombre (máximo 200 caracteres) y entre con su correo institucional.'),
    ('registration.email.not-verified', 'Primero confirme su correo con el enlace que le enviamos y vuelva a intentarlo.'),
    ('registration.domain.not-allowed', 'El dominio de su correo no pertenece a ninguna institución activa en BookHive.'),
    ('registration.already-registered', 'Esa cuenta ya está registrada en BookHive. Inicie sesión.'),
    ('data-policy.public.not-found', 'No encontramos el aviso de tratamiento de datos para ese correo. Revise que sea su correo institucional.');
