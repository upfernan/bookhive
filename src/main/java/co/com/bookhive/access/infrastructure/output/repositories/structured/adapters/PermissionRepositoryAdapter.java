package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.SchemaName;
import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantContext;
import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantSession;
import co.com.bookhive.access.services.output.ports.repositories.PermissionRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class PermissionRepositoryAdapter implements PermissionRepository {

	private static final String GRANT_QUERY =
			"SELECT 1 AS granted FROM person_role pr"
					+ " JOIN role_permission rp ON rp.role_id = pr.role_id"
					+ " WHERE pr.person_id = ? AND rp.permission_code = ? LIMIT 1";

	private final DataSource dataSource;

	public PermissionRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public boolean isGranted(final UUID person, final String permission) {

		if (ObjectHelper.isNull(person) || TextHelper.isDefaultValue(permission)) {
			return false;
		}

		final String schema = SchemaName.requireValid(TenantContext.get());

		try (Connection connection = dataSource.getConnection()) {

			TenantSession.enter(connection, schema);

			try {
				return hasGrant(connection, person, permission.trim());
			} finally {
				TenantSession.leave(connection);
			}

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.REQUEST_UNEXPECTED,
					"Falló la consulta de permisos de " + person + " en el esquema " + schema + ".");
		}
	}

	private static boolean hasGrant(final Connection connection, final UUID person, final String permission)
			throws SQLException {

		try (PreparedStatement statement = connection.prepareStatement(GRANT_QUERY)) {

			statement.setObject(1, person);
			statement.setString(2, permission);

			try (ResultSet result = statement.executeQuery()) {
				return result.next();
			}
		}
	}

}
