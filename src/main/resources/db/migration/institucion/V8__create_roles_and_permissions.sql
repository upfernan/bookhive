CREATE TABLE permission (
    code         VARCHAR(100) NOT NULL,
    description  VARCHAR(200) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_permission PRIMARY KEY (code)
);

CREATE TABLE role (
    id          UUID        NOT NULL,
    name        VARCHAR(50) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_role       PRIMARY KEY (id),
    CONSTRAINT uq_role_name  UNIQUE (name)
);

CREATE TABLE role_permission (
    role_id          UUID         NOT NULL,
    permission_code  VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_role_permission             PRIMARY KEY (role_id, permission_code),
    CONSTRAINT fk_role_permission_role        FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT fk_role_permission_permission  FOREIGN KEY (permission_code) REFERENCES permission (code)
);

CREATE TABLE person_role (
    person_id   UUID        NOT NULL,
    role_id     UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_person_role         PRIMARY KEY (person_id, role_id),
    CONSTRAINT fk_person_role_person  FOREIGN KEY (person_id) REFERENCES compartido.persona (id),
    CONSTRAINT fk_person_role_role    FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE INDEX ix_person_role_role ON person_role (role_id);

INSERT INTO permission (code, description) VALUES
    ('loan.lend', 'Registrar el préstamo de un ejemplar');

INSERT INTO role (id, name) VALUES
    (gen_random_uuid(), 'ADMINISTRADOR'),
    (gen_random_uuid(), 'BIBLIOTECARIO'),
    (gen_random_uuid(), 'LECTOR');
