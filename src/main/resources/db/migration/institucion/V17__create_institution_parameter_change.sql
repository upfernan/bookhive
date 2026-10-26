CREATE TABLE institution_parameter_change (
    id              UUID         NOT NULL,
    parameter       VARCHAR(40)  NOT NULL,
    previous_value  VARCHAR(64)  NOT NULL,
    new_value       VARCHAR(64)  NOT NULL,
    changed_by      UUID         NOT NULL,
    changed_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_institution_parameter_change            PRIMARY KEY (id),
    CONSTRAINT ck_institution_parameter_change_parameter  CHECK (parameter IN
        ('loan_days', 'max_concurrent_loans', 'daily_fine_rate', 'time_zone'))
);

CREATE INDEX ix_institution_parameter_change_changed_at ON institution_parameter_change (changed_at);

GRANT INSERT ON institution_parameter_change TO inst_${flyway:defaultSchema};

REVOKE UPDATE, DELETE, TRUNCATE ON institution_parameter_change FROM inst_${flyway:defaultSchema};

GRANT UPDATE (loan_days, max_concurrent_loans, daily_fine_rate, time_zone)
    ON institution_parameters TO inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('institution.configure', 'Cambiar las reglas de préstamo y multas de la institución');
