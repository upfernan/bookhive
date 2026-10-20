package co.com.bookhive.initializer.migrations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MigrationsMain {

	private static final Logger logger = LoggerFactory.getLogger(MigrationsMain.class);

	private static final String SHARED_SCHEMA = "compartido";
	private static final String SHARED_PATH = "classpath:db/migration/compartido";
	private static final String INSTITUTION_PATH = "classpath:db/migration/institucion";

	private static final Pattern SCHEMA_NAME = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

	private MigrationsMain() {
		super();
	}

	public static void main(final String[] args) {

		final String url = requiredVariable("DB_URL");
		final String user = requiredVariable("DB_ADMIN_USERNAME");
		final String password = requiredVariable("DB_ADMIN_PASSWORD");
		final Map<String, String> placeholders =
				MigrationPlaceholders.forApplicationUser(System.getenv("DB_APP_USERNAME"));

		try {
			migrateSharedSchema(url, user, password, placeholders);

			final List<String> schemas = readInstitutionSchemas(url, user, password);
			logger.info("Instituciones activas: {}", schemas.size());

			migrateInstitutions(url, user, password, placeholders, schemas);

		} catch (final Exception exception) {
			logger.error("Las migraciones fallaron.", exception);
			System.exit(1);
		}
	}

	private static void migrateSharedSchema(
			final String url, final String user, final String password,
			final Map<String, String> placeholders) {

		Flyway.configure()
				.dataSource(url, user, password)
				.placeholders(placeholders)
				.schemas(SHARED_SCHEMA)
				.locations(SHARED_PATH)
				.createSchemas(true)
				.load()
				.migrate();

		logger.info("Esquema compartido al día.");
	}

	private static List<String> readInstitutionSchemas(
			final String url, final String user, final String password) throws Exception {

		final List<String> schemas = new ArrayList<>();

		final String query =
				"SELECT esquema FROM " + SHARED_SCHEMA + ".institucion"
				+ " WHERE estado = 'ACTIVA' ORDER BY esquema";

		try (Connection connection = DriverManager.getConnection(url, user, password);
			 PreparedStatement statement = connection.prepareStatement(query);
			 ResultSet result = statement.executeQuery()) {

			while (result.next()) {
				schemas.add(result.getString("esquema"));
			}
		}

		return schemas;
	}

	private static void migrateInstitutions(
			final String url, final String user, final String password,
			final Map<String, String> placeholders, final List<String> schemas) {

		final List<String> failed = new ArrayList<>();

		for (final String schema : schemas) {

			if (!SCHEMA_NAME.matcher(schema).matches()) {
				failed.add(schema + " (nombre de esquema no válido)");
				continue;
			}

			try {
				Flyway.configure()
						.dataSource(url, user, password)
						.placeholders(placeholders)
						.schemas(schema)
						.locations(INSTITUTION_PATH)
						.createSchemas(true)
						.load()
						.migrate();

				logger.info("Esquema {} al día.", schema);

			} catch (final RuntimeException exception) {
				failed.add(schema + " (" + exception.getMessage() + ")");
				logger.error("El esquema {} FALLÓ.", schema, exception);
			}
		}

		if (!failed.isEmpty()) {
			throw new IllegalStateException(
					"Quedaron instituciones sin migrar: " + String.join(", ", failed));
		}
	}

	private static String requiredVariable(final String name) {
		final String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			logger.error("Falta la variable de entorno {}.", name);
			System.exit(1);
		}
		return value;
	}

}
