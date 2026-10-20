ALTER DEFAULT PRIVILEGES IN SCHEMA ${flyway:defaultSchema} REVOKE SELECT ON TABLES FROM ${appUser};

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA ${flyway:defaultSchema} FROM ${appUser};

REVOKE USAGE ON SCHEMA ${flyway:defaultSchema} FROM ${appUser};
