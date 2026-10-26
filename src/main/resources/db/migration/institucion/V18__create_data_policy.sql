CREATE TABLE data_policy (
    version       INTEGER      NOT NULL,
    content       TEXT         NOT NULL,
    published_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_data_policy          PRIMARY KEY (version),
    CONSTRAINT ck_data_policy_version  CHECK (version > 0),
    CONSTRAINT ck_data_policy_content  CHECK (length(trim(content)) > 0)
);

CREATE TABLE data_policy_acceptance (
    person_id       UUID         NOT NULL,
    policy_version  INTEGER      NOT NULL,
    accepted_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_data_policy_acceptance         PRIMARY KEY (person_id, policy_version),
    CONSTRAINT fk_data_policy_acceptance_person  FOREIGN KEY (person_id) REFERENCES compartido.persona (id),
    CONSTRAINT fk_data_policy_acceptance_policy  FOREIGN KEY (policy_version) REFERENCES data_policy (version)
);

GRANT INSERT ON data_policy_acceptance TO inst_${flyway:defaultSchema};

REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON data_policy FROM inst_${flyway:defaultSchema};

REVOKE UPDATE, DELETE, TRUNCATE ON data_policy_acceptance FROM inst_${flyway:defaultSchema};

INSERT INTO data_policy (version, content) VALUES (1,
'La institución es la responsable del tratamiento de sus datos personales dentro de BookHive, conforme a la Ley 1581 de 2012 y el Decreto 1377 de 2013.

Datos que se tratan: su nombre, su correo institucional y el registro de sus préstamos, devoluciones, reservas, multas y pagos.

Para qué se usan: prestarle material de la biblioteca, llevar la fila de reservas, calcular y cobrar las multas por devolución tardía, y enviarle avisos sobre sus préstamos, reservas y pagos. No se venden ni se ceden a terceros. Los pagos en línea los procesa la pasarela de pagos, que recibe solo lo necesario para cobrar.

Sus derechos: conocer, actualizar y corregir sus datos, pedir prueba de esta autorización, saber cómo se han usado, revocar la autorización y pedir que se eliminen cuando no exista una obligación legal o contractual de conservarlos. Puede ejercerlos ante la biblioteca de su institución.

Al eliminar sus datos se borran su nombre y su correo; el registro de préstamos, multas y pagos se conserva sin identificarlo, porque sirve de soporte contable.

Al aceptar, autoriza a la institución a tratar sus datos para estos fines.');
