DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '${appUser}') THEN
        CREATE ROLE ${appUser} LOGIN;
    END IF;
END
$$;

GRANT USAGE ON SCHEMA compartido TO ${appUser};

GRANT SELECT ON institucion, mensaje, persona TO ${appUser};
