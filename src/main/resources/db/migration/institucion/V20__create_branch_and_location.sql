CREATE TABLE branch (
    id          UUID          NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT pk_branch       PRIMARY KEY (id),
    CONSTRAINT ck_branch_name  CHECK (length(trim(name)) > 0)
);

CREATE UNIQUE INDEX uq_branch_name ON branch (lower(name));

INSERT INTO branch (id, name)
SELECT branch_id, 'Sede ' || row_number() OVER (ORDER BY branch_id)
FROM (SELECT DISTINCT branch_id FROM book_copy) existing;

ALTER TABLE book_copy ADD CONSTRAINT fk_book_copy_branch FOREIGN KEY (branch_id) REFERENCES branch (id);

CREATE TABLE location_level (
    depth  SMALLINT     NOT NULL,
    name   VARCHAR(40)  NOT NULL,

    CONSTRAINT pk_location_level        PRIMARY KEY (depth),
    CONSTRAINT ck_location_level_depth  CHECK (depth BETWEEN 1 AND 6),
    CONSTRAINT ck_location_level_name   CHECK (length(trim(name)) > 0)
);

INSERT INTO location_level (depth, name) VALUES (1, 'Piso'), (2, 'Estante'), (3, 'Posición');

CREATE TABLE book_copy_location (
    book_copy_id  UUID         NOT NULL,
    depth         SMALLINT     NOT NULL,
    value         VARCHAR(40)  NOT NULL,

    CONSTRAINT pk_book_copy_location        PRIMARY KEY (book_copy_id, depth),
    CONSTRAINT fk_book_copy_location_copy   FOREIGN KEY (book_copy_id) REFERENCES book_copy (id),
    CONSTRAINT fk_book_copy_location_level  FOREIGN KEY (depth) REFERENCES location_level (depth),
    CONSTRAINT ck_book_copy_location_value  CHECK (length(trim(value)) > 0)
);

CREATE INDEX ix_book_copy_location_place ON book_copy_location (depth, value);

GRANT INSERT, UPDATE ON branch TO inst_${flyway:defaultSchema};

GRANT INSERT, UPDATE, DELETE ON location_level, book_copy_location TO inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('book-copy.locate', 'Ver la ubicación completa de un ejemplar dentro de la biblioteca'),
    ('book-copy.relocate', 'Cambiar la sede o la ubicación de un ejemplar'),
    ('book-copy.withdraw', 'Dar de baja un ejemplar de la biblioteca');
