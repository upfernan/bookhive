CREATE TABLE category (
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_category       PRIMARY KEY (id),
    CONSTRAINT uq_category_name  UNIQUE (name)
);

CREATE TABLE publisher (
    id          UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_publisher       PRIMARY KEY (id),
    CONSTRAINT uq_publisher_name  UNIQUE (name)
);

CREATE TABLE author (
    id          UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_author PRIMARY KEY (id)
);

CREATE TABLE book (
    id                UUID         NOT NULL,
    title             VARCHAR(300) NOT NULL,
    isbn              VARCHAR(17),
    publication_year  SMALLINT,
    format            VARCHAR(20)  NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    category_id       UUID         NOT NULL,
    publisher_id      UUID         NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_book            PRIMARY KEY (id),
    CONSTRAINT uq_book_isbn       UNIQUE (isbn),
    CONSTRAINT fk_book_category   FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_book_publisher  FOREIGN KEY (publisher_id) REFERENCES publisher (id),
    CONSTRAINT ck_book_format     CHECK (format IN ('FISICO', 'DIGITAL')),
    CONSTRAINT ck_book_status     CHECK (status IN ('ACTIVO', 'BAJA'))
);

CREATE INDEX ix_book_category ON book (category_id);
CREATE INDEX ix_book_publisher ON book (publisher_id);

CREATE TABLE book_author (
    book_id     UUID        NOT NULL,
    author_id   UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_book_author         PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_author_book    FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_book_author_author  FOREIGN KEY (author_id) REFERENCES author (id)
);

CREATE INDEX ix_book_author_author ON book_author (author_id);

ALTER TABLE book_copy
    ADD CONSTRAINT fk_book_copy_book FOREIGN KEY (book_id) REFERENCES book (id) NOT VALID;

CREATE INDEX ix_book_copy_book_status ON book_copy (book_id, status);

CREATE TABLE catalog_change (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    book_id     UUID        NOT NULL,
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    sent_at     TIMESTAMPTZ,

    CONSTRAINT pk_catalog_change PRIMARY KEY (id)
);

CREATE INDEX ix_catalog_change_pending ON catalog_change (changed_at) WHERE sent_at IS NULL;

CREATE FUNCTION note_catalog_change() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF TG_TABLE_NAME = 'book' AND TG_OP = 'DELETE' THEN
        INSERT INTO ${flyway:defaultSchema}.catalog_change (book_id) VALUES (OLD.id);
    ELSIF TG_TABLE_NAME = 'book' THEN
        INSERT INTO ${flyway:defaultSchema}.catalog_change (book_id) VALUES (NEW.id);
    ELSIF TG_TABLE_NAME = 'book_author' AND TG_OP = 'DELETE' THEN
        INSERT INTO ${flyway:defaultSchema}.catalog_change (book_id) VALUES (OLD.book_id);
    ELSIF TG_TABLE_NAME = 'book_author' THEN
        INSERT INTO ${flyway:defaultSchema}.catalog_change (book_id) VALUES (NEW.book_id);
    ELSIF TG_TABLE_NAME = 'author' THEN
        INSERT INTO ${flyway:defaultSchema}.catalog_change (book_id)
            SELECT book_id FROM ${flyway:defaultSchema}.book_author WHERE author_id = NEW.id;
    ELSIF TG_TABLE_NAME = 'category' THEN
        INSERT INTO ${flyway:defaultSchema}.catalog_change (book_id)
            SELECT id FROM ${flyway:defaultSchema}.book WHERE category_id = NEW.id;
    ELSIF TG_TABLE_NAME = 'publisher' THEN
        INSERT INTO ${flyway:defaultSchema}.catalog_change (book_id)
            SELECT id FROM ${flyway:defaultSchema}.book WHERE publisher_id = NEW.id;
    END IF;
    RETURN NULL;
END
$$;

CREATE TRIGGER tg_book_catalog_change
    AFTER INSERT OR UPDATE OR DELETE ON book
    FOR EACH ROW EXECUTE FUNCTION note_catalog_change();

CREATE TRIGGER tg_book_author_catalog_change
    AFTER INSERT OR UPDATE OR DELETE ON book_author
    FOR EACH ROW EXECUTE FUNCTION note_catalog_change();

CREATE TRIGGER tg_author_catalog_change
    AFTER UPDATE ON author
    FOR EACH ROW EXECUTE FUNCTION note_catalog_change();

CREATE TRIGGER tg_category_catalog_change
    AFTER UPDATE ON category
    FOR EACH ROW EXECUTE FUNCTION note_catalog_change();

CREATE TRIGGER tg_publisher_catalog_change
    AFTER UPDATE ON publisher
    FOR EACH ROW EXECUTE FUNCTION note_catalog_change();

GRANT UPDATE (sent_at) ON catalog_change TO inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('catalog.search', 'Buscar libros en el catálogo'),
    ('catalog.sync', 'Actualizar el buscador del catálogo');
