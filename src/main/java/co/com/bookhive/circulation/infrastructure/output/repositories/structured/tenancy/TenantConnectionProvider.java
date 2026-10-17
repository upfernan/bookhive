package co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

	private static final long serialVersionUID = 4625192699836216657L;

	private final transient DataSource dataSource;

	public TenantConnectionProvider(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void releaseAnyConnection(final Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public Connection getConnection(final String schema) throws SQLException {

		final Connection connection = dataSource.getConnection();
		useSchema(connection, schema);

		return connection;
	}

	@Override
	public void releaseConnection(final String schema, final Connection connection)
			throws SQLException {

		try {
			useSchema(connection, TenantContext.SCHEMA_WITHOUT_TENANT);
		} finally {
			connection.close();
		}
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	public boolean handlesConnectionSchema() {
		return true;
	}

	@Override
	public boolean isUnwrappableAs(final Class<?> type) {
		return false;
	}

	@Override
	public <T> T unwrap(final Class<T> type) {
		return null;
	}

	private static void useSchema(final Connection connection, final String schema)
			throws SQLException {

		try (Statement statement = connection.createStatement()) {
			statement.execute("SET search_path TO " + SchemaName.requireValid(schema));
		}
	}

}
