ALTER TABLE library_user ADD COLUMN deactivated_at TIMESTAMPTZ;

ALTER TABLE library_user DROP CONSTRAINT ck_library_user_status;

ALTER TABLE library_user ADD CONSTRAINT ck_library_user_status
    CHECK (status IN ('ACTIVO', 'SUSPENDIDO', 'RETIRADO', 'DEPURADO'));

ALTER TABLE institution_parameters ADD COLUMN retention_months INTEGER NOT NULL DEFAULT 24;

ALTER TABLE institution_parameters ADD CONSTRAINT ck_institution_parameters_retention
    CHECK (retention_months BETWEEN 1 AND 120);

ALTER TABLE institution_parameter_change DROP CONSTRAINT ck_institution_parameter_change_parameter;

ALTER TABLE institution_parameter_change ADD CONSTRAINT ck_institution_parameter_change_parameter CHECK (parameter IN
    ('loan_days', 'max_concurrent_loans', 'daily_fine_rate', 'time_zone', 'retention_months'));

GRANT UPDATE (name, email, status, deactivated_at) ON library_user TO inst_${flyway:defaultSchema};

GRANT UPDATE (retention_months) ON institution_parameters TO inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('user.manage', 'Dar de baja, reactivar y depurar las cuentas de los usuarios');
