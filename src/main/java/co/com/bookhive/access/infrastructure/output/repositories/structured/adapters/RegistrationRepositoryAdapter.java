package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantSession;
import co.com.bookhive.access.services.output.ports.repositories.RegistrationRepository;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities.DomainInstitution;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities.NewPerson;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities.PublicPolicy;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class RegistrationRepositoryAdapter implements RegistrationRepository {

	private static final String UNIQUE_VIOLATION = "23505";
	private static final String FIND_DOMAIN = "SELECT i.id, i.esquema FROM compartido.dominio_institucion d"
			+ " JOIN compartido.institucion i ON i.id = d.institucion_id WHERE d.dominio = ? AND i.estado = 'ACTIVA'";
	private static final String FIND_IDENTITY = "SELECT id FROM compartido.persona WHERE identidad = ?";
	private static final String FIND_PERSON = "SELECT id FROM compartido.persona WHERE id = ?";
	private static final String FIND_USER = "SELECT id FROM library_user WHERE lower(email) = lower(?)";
	private static final String INSERT_PERSON =
			"INSERT INTO compartido.persona (id, identidad, institucion_id, estado) VALUES (?, ?, ?, 'ACTIVA')";
	private static final String INSERT_USER = "INSERT INTO library_user (id, name, email, status) VALUES (?, ?, ?, 'ACTIVO')";
	private static final String INSERT_ROLE = "INSERT INTO person_role (person_id, role_id)"
			+ " SELECT ?, id FROM role WHERE name = 'LECTOR' ON CONFLICT DO NOTHING";
	private static final String CURRENT_POLICY = "SELECT coalesce(max(version), 0) FROM data_policy";
	private static final String CURRENT_POLICY_TEXT =
			"SELECT version, content, published_at FROM data_policy ORDER BY version DESC LIMIT 1";
	private static final String INSERT_ACCEPTANCE = "INSERT INTO data_policy_acceptance (person_id, policy_version)"
			+ " VALUES (?, ?) ON CONFLICT DO NOTHING";
	private static final String CHANGE_ROLE = "SELECT set_config('role', ?, true)";
	private static final String CHANGE_PATH = "SELECT set_config('search_path', ?, true)";
	private static final String NO_ROLE = "none";

	private final DataSource dataSource;

	public RegistrationRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public Optional<DomainInstitution> institutionOfDomain(final String domain) {
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(FIND_DOMAIN)) {
			statement.setString(1, domain);
			try (ResultSet result = statement.executeQuery()) {
				return result.next()
						? Optional.of(new DomainInstitution(result.getObject(1, UUID.class), result.getString(2)))
						: Optional.empty();
			}
		} catch (final SQLException exception) {
			throw failure(exception, "Falló la búsqueda del dominio " + domain + ".");
		}
	}

	@Override
	public boolean identityRegistered(final String identity) {
		try (Connection connection = dataSource.getConnection()) {
			return exists(connection, FIND_IDENTITY, identity);
		} catch (final SQLException exception) {
			throw failure(exception, "Falló la búsqueda de una identidad.");
		}
	}

	@Override
	public Optional<PublicPolicy> currentPolicyOf(final DomainInstitution institution) {
		try (Connection connection = dataSource.getConnection()) {
			return policyWithin(connection, institution.schema());
		} catch (final SQLException exception) {
			throw failure(exception, "Falló la consulta del aviso de datos de " + institution.schema() + ".");
		}
	}

	private static Optional<PublicPolicy> policyWithin(final Connection connection, final String schema) throws SQLException {
		connection.setAutoCommit(false);
		try {
			enterInstitution(connection, schema);
			return readPolicy(connection);
		} finally {
			connection.rollback();
			TenantSession.leave(connection);
		}
	}

	private static Optional<PublicPolicy> readPolicy(final Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(CURRENT_POLICY_TEXT);
			 ResultSet result = statement.executeQuery()) {
			return result.next()
					? Optional.of(new PublicPolicy(result.getInt(1), result.getString(2),
							result.getObject(3, OffsetDateTime.class).toInstant()))
					: Optional.empty();
		}
	}

	@Override
	public NewPerson register(final NewPerson person, final DomainInstitution institution) {
		try (Connection connection = dataSource.getConnection()) {
			return registerWithin(connection, person, institution);
		} catch (final SQLException exception) {
			throw failure(exception, "Falló el registro de una persona en " + institution.schema() + ".");
		}
	}

	private static NewPerson registerWithin(
			final Connection connection, final NewPerson person, final DomainInstitution institution) throws SQLException {
		connection.setAutoCommit(false);
		try {
			final NewPerson registered = insert(connection, person, institution);
			connection.commit();
			return registered;
		} catch (final SQLException exception) {
			connection.rollback();
			if (UNIQUE_VIOLATION.equals(exception.getSQLState())) {
				throw BookHiveException.createConflict(MessageKey.REGISTRATION_ALREADY_REGISTERED,
						"La identidad o el correo ya estaban registrados en " + institution.schema() + ".");
			}
			throw exception;
		} catch (final RuntimeException exception) {
			connection.rollback();
			throw exception;
		} finally {
			TenantSession.leave(connection);
		}
	}

	private static NewPerson insert(final Connection connection, final NewPerson person, final DomainInstitution institution)
			throws SQLException {

		enterInstitution(connection, institution.schema());
		requireCurrentPolicy(connection, person.policyVersion());
		final Optional<UUID> existingUser = first(connection, FIND_USER, person.email());
		leaveInstitution(connection);

		final UUID id = existingUser.orElse(UUIDHelper.getDefaultValue(person.id()));
		if (existingUser.isPresent() && exists(connection, FIND_PERSON, id)) {
			throw BookHiveException.createConflict(MessageKey.REGISTRATION_ALREADY_REGISTERED,
					"El correo de la persona " + id + " ya tiene una cuenta.");
		}
		update(connection, INSERT_PERSON, id, person.identity(), institution.institution());

		enterInstitution(connection, institution.schema());
		if (existingUser.isEmpty()) {
			update(connection, INSERT_USER, id, person.name(), person.email());
		}
		update(connection, INSERT_ROLE, id);
		update(connection, INSERT_ACCEPTANCE, id, person.policyVersion());
		return new NewPerson(id, person.identity(), person.name(), person.email(), person.policyVersion());
	}

	private static void requireCurrentPolicy(final Connection connection, final int accepted) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(CURRENT_POLICY);
			 ResultSet result = statement.executeQuery()) {
			final int current = result.next() ? result.getInt(1) : 0;
			if (current != accepted) {
				throw BookHiveException.createConflict(MessageKey.DATA_POLICY_VERSION_OUTDATED,
						"Se aceptó la versión " + accepted + " del aviso y la vigente es la " + current + ".");
			}
		}
	}

	private static void enterInstitution(final Connection connection, final String schema) throws SQLException {
		final String role = TenantSession.roleOf(schema);
		update(connection, CHANGE_ROLE, role);
		update(connection, CHANGE_PATH, schema);
	}

	private static void leaveInstitution(final Connection connection) throws SQLException {
		update(connection, CHANGE_ROLE, NO_ROLE);
	}

	private static boolean exists(final Connection connection, final String sql, final Object value) throws SQLException {
		return first(connection, sql, value).isPresent();
	}

	private static Optional<UUID> first(final Connection connection, final String sql, final Object value) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, value);
			try (ResultSet result = statement.executeQuery()) {
				return result.next() ? Optional.of(result.getObject(1, UUID.class)) : Optional.empty();
			}
		}
	}

	private static void update(final Connection connection, final String sql, final Object... values) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			for (int position = 0; position < values.length; position++) {
				statement.setObject(position + 1, values[position]);
			}
			statement.execute();
		}
	}

	private static BookHiveException failure(final SQLException exception, final String message) {
		return BookHiveException.createUnexpected(exception, MessageKey.REQUEST_UNEXPECTED, message);
	}

}
