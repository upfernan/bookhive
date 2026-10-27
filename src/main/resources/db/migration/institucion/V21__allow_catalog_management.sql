ALTER TABLE category ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVA';

ALTER TABLE category ADD CONSTRAINT ck_category_status CHECK (status IN ('ACTIVA', 'INACTIVA'));

ALTER TABLE category DROP CONSTRAINT uq_category_name;

CREATE UNIQUE INDEX uq_category_name ON category (lower(name));

GRANT INSERT, UPDATE ON category, publisher, author, book TO inst_${flyway:defaultSchema};

GRANT INSERT, DELETE ON book_author TO inst_${flyway:defaultSchema};

GRANT INSERT ON book_copy, catalog_change TO inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('catalog.manage', 'Registrar, editar y dar de baja títulos del catálogo'),
    ('category.manage', 'Crear, editar, desactivar y activar las categorías del catálogo');
