package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.SchemaName;
import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantSession;
import co.com.bookhive.access.services.output.ports.repositories.PermissionRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PermissionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.PermissionEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

@Component
public class PermissionRepositoryAdapter implements PermissionRepository {

	private static final String FIND_GRANTED_PERMISSIONS =
			"SELECT DISTINCT rp.permission_code FROM person_role pr"
					+ " JOIN role_permission rp ON rp.role_id = pr.role_id";

	private final DataSource dataSource;

	public PermissionRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public List<PermissionEntity> findByCriteria(final PermissionCriteria criteria) {

		if (ObjectHelper.isNull(criteria)) {
			return List.of();
		}

		final SqlFilter filter = new SqlFilter(FIND_GRANTED_PERMISSIONS);

		if (criteria.filtersByPerson()) {
			filter.add("pr.person_id = ?", criteria.getPerson());
		}
		if (criteria.filtersByCode()) {
			filter.add("rp.permission_code = ?", criteria.getCode());
		}

		final String schema = SchemaName.requireValid(TenantContext.get());

		try (Connection connection = dataSource.getConnection()) {

			TenantSession.enter(connection, schema);

			try (PreparedStatement statement = connection.prepareStatement(filter.query())) {
				filter.bind(statement);
				return readPermissions(statement);
			} finally {
				TenantSession.leave(connection);
			}

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.REQUEST_UNEXPECTED,
					"Falló la consulta de permisos de " + criteria.getPerson() + " en el esquema " + schema + ".");
		}
	}

	private static List<PermissionEntity> readPermissions(final PreparedStatement statement)
			throws SQLException {

		final List<PermissionEntity> permissions = new ArrayList<>();

		try (ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				final PermissionEntity permission = new PermissionEntity();
				permission.setCode(result.getString("permission_code"));
				permissions.add(permission);
			}
		}

		return permissions;
	}

}
