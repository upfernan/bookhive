package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

final class SqlFilter {

	private final StringBuilder query;
	private final List<Object> values = new ArrayList<>();

	SqlFilter(final String baseQuery) {
		this.query = new StringBuilder(baseQuery);
	}

	void add(final String condition, final Object value) {
		query.append(values.isEmpty() ? " WHERE " : " AND ").append(condition);
		values.add(value);
	}

	String query() {
		return query.toString();
	}

	void bind(final PreparedStatement statement) throws SQLException {
		for (int position = 0; position < values.size(); position++) {
			statement.setObject(position + 1, values.get(position));
		}
	}

}
