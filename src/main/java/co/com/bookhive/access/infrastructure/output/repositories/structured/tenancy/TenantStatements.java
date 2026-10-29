package co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

public final class TenantStatements {

	@FunctionalInterface
	public interface Work<T> {

		T run(PreparedStatement statement) throws SQLException;

	}

	private TenantStatements() {
		super();
	}

	public static <T> T run(
			final DataSource dataSource,
			final String sql,
			final String failure,
			final Work<T> work) {

		final String schema = SchemaName.requireValid(TenantContext.get());

		try (Connection connection = dataSource.getConnection()) {

			TenantSession.enter(connection, schema);

			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				return work.run(statement);
			} finally {
				TenantSession.leave(connection);
			}

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception, MessageKey.REQUEST_UNEXPECTED, failure + " Esquema " + schema + ".");
		}
	}

}
