package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;
import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.SchemaName;
import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantSession;
import co.com.bookhive.access.services.output.ports.repositories.SessionProfileRepository;
import co.com.bookhive.access.services.output.ports.repositories.entities.SessionProfileEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

@Component
public class SessionProfileRepositoryAdapter implements SessionProfileRepository {

	private static final String INSTITUTION = "SELECT nombre FROM compartido.institucion WHERE id = ?";
	private static final String EMAIL = "SELECT email FROM library_user WHERE id = ?";
	private static final String ROLES =
			"SELECT r.name FROM person_role pr JOIN role r ON r.id = pr.role_id WHERE pr.person_id = ? ORDER BY r.name";

	private final DataSource dataSource;

	public SessionProfileRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public SessionProfileEntity findProfile(final UUID person, final UUID institution) {

		final String schema = SchemaName.requireValid(TenantContext.get());

		try (Connection connection = dataSource.getConnection()) {

			final String institutionName = first(connection, INSTITUTION, institution);
			TenantSession.enter(connection, schema);

			try {
				return new SessionProfileEntity(first(connection, EMAIL, person), institutionName, all(connection, ROLES, person));
			} finally {
				TenantSession.leave(connection);
			}

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception, MessageKey.REQUEST_UNEXPECTED, "Falló la consulta del perfil de " + person + " en " + schema + ".");
		}
	}

	private static String first(final Connection connection, final String sql, final UUID id) throws SQLException {
		final List<String> found = all(connection, sql, id);
		return found.isEmpty() ? "" : found.get(0);
	}

	private static List<String> all(final Connection connection, final String sql, final UUID id) throws SQLException {
		final List<String> values = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, id);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					values.add(result.getString(1));
				}
			}
		}
		return values;
	}

}
