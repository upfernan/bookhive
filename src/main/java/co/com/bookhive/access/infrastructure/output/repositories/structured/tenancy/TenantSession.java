package co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy;

import co.com.bookhive.crosscutting.tenancy.TenantContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class TenantSession {

	static final String ROLE_PREFIX = "inst_";

	private static final String CHANGE_SETTING = "SELECT set_config(?, ?, false)";
	private static final String ROLE = "role";
	private static final String SEARCH_PATH = "search_path";
	private static final String NO_ROLE = "none";

	private TenantSession() {
		super();
	}

	public static String roleOf(final String schema) {
		return ROLE_PREFIX + SchemaName.requireValid(schema);
	}

	public static void enter(final Connection connection, final String schema) throws SQLException {

		final String validSchema = SchemaName.requireValid(schema);

		change(connection, ROLE, roleOf(validSchema));
		change(connection, SEARCH_PATH, validSchema);
	}

	public static void leave(final Connection connection) throws SQLException {

		if (!connection.getAutoCommit()) {
			connection.rollback();
			connection.setAutoCommit(true);
		}

		change(connection, ROLE, NO_ROLE);
		change(connection, SEARCH_PATH, TenantContext.SCHEMA_WITHOUT_TENANT);
	}

	private static void change(final Connection connection, final String setting, final String value)
			throws SQLException {

		try (PreparedStatement statement = connection.prepareStatement(CHANGE_SETTING)) {
			statement.setString(1, setting);
			statement.setString(2, value);
			statement.execute();
		}
	}

}
