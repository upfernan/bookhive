package co.com.bookhive.access.testdata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public final class FakeDataSource {

	private FakeDataSource() {
		super();
	}

	public static DataSource withRow(final Map<String, Object> row) {
		return create(row, false);
	}

	public static DataSource withoutRows() {
		return create(Map.of(), false);
	}

	public static DataSource failing() {
		return create(Map.of(), true);
	}

	public static DataSource recording(final List<String> events) {
		final Statement statement = proxy(Statement.class, (instance, method, arguments) -> {
			if ("execute".equals(method.getName())) {
				events.add(String.valueOf(arguments[0]));
			}
			return defaultOf(method);
		});
		final Connection connection = proxy(Connection.class, (instance, method, arguments) -> {
			if ("close".equals(method.getName()) || "rollback".equals(method.getName())) {
				events.add(method.getName());
			}
			if ("getAutoCommit".equals(method.getName())) {
				return !events.contains("inTransaction");
			}
			if ("prepareStatement".equals(method.getName())) {
				return recordingSetting(events);
			}
			return "createStatement".equals(method.getName()) ? statement : defaultOf(method);
		});

		return proxy(DataSource.class,
				(instance, method, arguments) -> "getConnection".equals(method.getName()) ? connection : defaultOf(method));
	}

	private static PreparedStatement recordingSetting(final List<String> events) {
		final Object[] values = new Object[3];
		return proxy(PreparedStatement.class, (instance, method, arguments) -> {
			if ("setString".equals(method.getName())) {
				values[(int) arguments[0]] = arguments[1];
			}
			if ("execute".equals(method.getName())) {
				events.add("set " + values[1] + "=" + values[2]);
			}
			return defaultOf(method);
		});
	}

	private static DataSource create(final Map<String, Object> row, final boolean fails) {
		final ResultSet result = proxy(ResultSet.class, new ResultSetHandler(row));
		final PreparedStatement statement = proxy(PreparedStatement.class,
				(instance, method, arguments) -> "executeQuery".equals(method.getName()) ? result : defaultOf(method));
		final Connection connection = proxy(Connection.class,
				(instance, method, arguments) -> "prepareStatement".equals(method.getName()) ? statement : defaultOf(method));

		return proxy(DataSource.class, (instance, method, arguments) -> {
			if (!"getConnection".equals(method.getName())) {
				return defaultOf(method);
			}
			if (fails) {
				throw new SQLException("La base de datos no responde.");
			}
			return connection;
		});
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(FakeDataSource.class.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static Object defaultOf(final Method method) {
		final Class<?> type = method.getReturnType();
		if (boolean.class.equals(type)) {
			return false;
		}
		if (int.class.equals(type)) {
			return 0;
		}
		if (long.class.equals(type)) {
			return 0L;
		}
		return null;
	}

	private static final class ResultSetHandler implements InvocationHandler {

		private final Map<String, Object> row;
		private boolean read;

		private ResultSetHandler(final Map<String, Object> row) {
			this.row = row;
		}

		@Override
		public Object invoke(final Object instance, final Method method, final Object[] arguments) {
			return switch (method.getName()) {
				case "next" -> nextRow();
				case "getObject", "getString" -> row.get(arguments[0]);
				default -> defaultOf(method);
			};
		}

		private boolean nextRow() {
			final boolean hasRow = !read && !row.isEmpty();
			read = true;
			return hasRow;
		}

	}

}
