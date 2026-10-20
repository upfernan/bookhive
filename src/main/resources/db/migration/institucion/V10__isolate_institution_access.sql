DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'inst_${flyway:defaultSchema}') THEN
        CREATE ROLE inst_${flyway:defaultSchema} NOLOGIN;
    END IF;
END
$$;

GRANT USAGE ON SCHEMA ${flyway:defaultSchema} TO inst_${flyway:defaultSchema};

GRANT SELECT ON ALL TABLES IN SCHEMA ${flyway:defaultSchema} TO inst_${flyway:defaultSchema};

ALTER DEFAULT PRIVILEGES IN SCHEMA ${flyway:defaultSchema} GRANT SELECT ON TABLES TO inst_${flyway:defaultSchema};

GRANT INSERT ON loan, fine TO inst_${flyway:defaultSchema};

GRANT UPDATE ON book_copy TO inst_${flyway:defaultSchema};

REVOKE UPDATE, DELETE, TRUNCATE ON loan, fine FROM inst_${flyway:defaultSchema};

GRANT inst_${flyway:defaultSchema} TO ${appUser} WITH INHERIT FALSE, SET TRUE;
