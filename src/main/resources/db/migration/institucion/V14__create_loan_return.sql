ALTER TABLE institution_parameters
    ADD COLUMN daily_fine_rate NUMERIC(12,2) NOT NULL DEFAULT 1000.00;

ALTER TABLE institution_parameters
    ADD CONSTRAINT ck_institution_parameters_daily_fine_rate CHECK (daily_fine_rate >= 0);

CREATE TABLE loan_return (
    id            UUID        NOT NULL,
    loan_id       UUID        NOT NULL,
    return_date   DATE        NOT NULL,
    overdue_days  INTEGER     NOT NULL,
    librarian_id  UUID        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_loan_return              PRIMARY KEY (id),
    CONSTRAINT uq_loan_return_loan         UNIQUE (loan_id),
    CONSTRAINT fk_loan_return_loan         FOREIGN KEY (loan_id) REFERENCES loan (id),
    CONSTRAINT ck_loan_return_overdue_days CHECK (overdue_days >= 0)
);

GRANT INSERT ON loan_return TO inst_${flyway:defaultSchema};
GRANT UPDATE (status) ON loan TO inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('loan.return', 'Registrar la devolución de un ejemplar');

INSERT INTO role_permission (role_id, permission_code)
SELECT id, 'loan.return' FROM role WHERE name IN ('ADMINISTRADOR', 'BIBLIOTECARIO');
