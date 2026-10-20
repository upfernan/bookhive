GRANT USAGE ON SCHEMA ${flyway:defaultSchema} TO ${appUser};

GRANT SELECT ON ALL TABLES IN SCHEMA ${flyway:defaultSchema} TO ${appUser};

ALTER DEFAULT PRIVILEGES IN SCHEMA ${flyway:defaultSchema} GRANT SELECT ON TABLES TO ${appUser};

GRANT INSERT ON loan, fine TO ${appUser};

GRANT UPDATE ON book_copy TO ${appUser};

REVOKE UPDATE, DELETE, TRUNCATE ON loan, fine FROM ${appUser};
