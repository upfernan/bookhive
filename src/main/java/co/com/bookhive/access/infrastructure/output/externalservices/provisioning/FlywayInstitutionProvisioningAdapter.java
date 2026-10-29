package co.com.bookhive.access.infrastructure.output.externalservices.provisioning;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.SchemaName;
import co.com.bookhive.access.services.output.ports.InstitutionProvisioningPort;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities.DomainInstitution;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class FlywayInstitutionProvisioningAdapter implements InstitutionProvisioningPort {

	private static final String INSTITUTION_MIGRATIONS = "classpath:db/migration/institucion";
	private static final String FIND = "SELECT i.id, i.esquema FROM compartido.dominio_institucion d"
			+ " JOIN compartido.institucion i ON i.id = d.institucion_id WHERE d.dominio = ?";
	private static final String SCHEMA_TAKEN = "SELECT 1 FROM compartido.institucion WHERE esquema = ?";
	private static final String INSERT_INSTITUTION = "INSERT INTO compartido.institucion"
			+ " (id, nombre, esquema, estado, fecha_registro, alta) VALUES (?, ?, ?, 'APROVISIONANDO', CURRENT_DATE, 'AUTOREGISTRO')";
	private static final String INSERT_DOMAIN = "INSERT INTO compartido.dominio_institucion (dominio, institucion_id) VALUES (?, ?)";
	private static final String ACTIVATE = "UPDATE compartido.institucion SET estado = 'ACTIVA' WHERE id = ? AND estado = 'APROVISIONANDO'";
	private static final String UNIQUE_VIOLATION = "23505";
	private static final int MAX_SCHEMA = 60;

	private final String url;
	private final String username;
	private final String password;
	private final String applicationUser;

	public FlywayInstitutionProvisioningAdapter(
			@Value("${spring.datasource.url}") final String url,
			@Value("${bookhive.provisioning.username:}") final String username,
			@Value("${bookhive.provisioning.password:}") final String password,
			@Value("${spring.datasource.username}") final String applicationUser) {
		super();
		this.url = TextHelper.getDefaultValue(url);
		this.username = TextHelper.getDefaultValue(username);
		this.password = TextHelper.getDefaultValue(password);
		this.applicationUser = TextHelper.getDefaultValue(applicationUser);
	}

	@Override
	public DomainInstitution provision(final String domain) {
		if (username.isEmpty() || password.isEmpty()) {
			throw unavailable(null, "No está configurado el usuario que crea instituciones.");
		}
		try {
			final DomainInstitution institution = reserve(domain);
			Flyway.configure()
					.dataSource(url, username, password)
					.placeholders(Map.of("appUser", applicationUser))
					.schemas(SchemaName.requireValid(institution.schema()))
					.locations(INSTITUTION_MIGRATIONS)
					.createSchemas(true)
					.load()
					.migrate();
			update(ACTIVATE, institution.institution());
			return institution;
		} catch (final SQLException | RuntimeException exception) {
			throw unavailable(exception, "Falló la creación de la institución de " + domain + ".");
		}
	}

	private DomainInstitution reserve(final String domain) throws SQLException {
		try (Connection connection = DriverManager.getConnection(url, username, password)) {
			final Optional<DomainInstitution> existing = find(connection, domain);
			if (existing.isPresent()) {
				return existing.get();
			}
			return insert(connection, domain);
		}
	}

	private static DomainInstitution insert(final Connection connection, final String domain) throws SQLException {
		final UUID id = UUIDHelper.generateNew();
		final String schema = freeSchema(connection, schemaOf(domain));
		connection.setAutoCommit(false);
		try {
			execute(connection, INSERT_INSTITUTION, id, nameOf(domain), schema);
			execute(connection, INSERT_DOMAIN, domain, id);
			connection.commit();
			return new DomainInstitution(id, schema);
		} catch (final SQLException exception) {
			connection.rollback();
			if (UNIQUE_VIOLATION.equals(exception.getSQLState())) {
				connection.setAutoCommit(true);
				return find(connection, domain).orElseThrow(() -> exception);
			}
			throw exception;
		}
	}

	public static String schemaOf(final String domain) {
		final String base = TextHelper.getDefaultValue(domain).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "_");
		final String named = startsWithLetter(base) ? base : "i_" + base;
		return named.length() > MAX_SCHEMA ? named.substring(0, MAX_SCHEMA) : named;
	}

	private static boolean startsWithLetter(final String value) {
		return !value.isEmpty() && Character.isLetter(value.charAt(0));
	}

	public static String nameOf(final String domain) {
		final String first = TextHelper.getDefaultValue(domain).split("\\.")[0];
		return first.toUpperCase(Locale.ROOT);
	}

	private static String freeSchema(final Connection connection, final String base) throws SQLException {
		String candidate = base;
		for (int suffix = 2; taken(connection, candidate); suffix++) {
			candidate = base + "_" + suffix;
		}
		return candidate;
	}

	private static boolean taken(final Connection connection, final String schema) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(SCHEMA_TAKEN)) {
			statement.setString(1, schema);
			try (ResultSet result = statement.executeQuery()) {
				return result.next();
			}
		}
	}

	private static Optional<DomainInstitution> find(final Connection connection, final String domain) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(FIND)) {
			statement.setString(1, domain);
			try (ResultSet result = statement.executeQuery()) {
				return result.next()
						? Optional.of(new DomainInstitution(result.getObject(1, UUID.class), result.getString(2)))
						: Optional.empty();
			}
		}
	}

	private void update(final String sql, final Object... values) throws SQLException {
		try (Connection connection = DriverManager.getConnection(url, username, password)) {
			execute(connection, sql, values);
		}
	}

	private static void execute(final Connection connection, final String sql, final Object... values) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			for (int position = 0; position < values.length; position++) {
				statement.setObject(position + 1, values[position]);
			}
			statement.executeUpdate();
		}
	}

	private static BookHiveException unavailable(final Exception cause, final String technicalMessage) {
		return BookHiveException.createUnavailable(
				cause == null ? new IllegalStateException(technicalMessage) : cause,
				MessageKey.REGISTRATION_INSTITUTION_UNAVAILABLE, technicalMessage);
	}

}
