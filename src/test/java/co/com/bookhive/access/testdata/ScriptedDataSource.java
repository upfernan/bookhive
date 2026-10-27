package co.com.bookhive.access.testdata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.sql.DataSource;

public final class ScriptedDataSource {

	public record Call(String sql, List<Object> values) {
	}

	private record Rule(String fragment, Function<List<Object>, Object> answer) {
	}

	private final List<Rule> rules = new ArrayList<>();
	private final List<Call> calls = new ArrayList<>();
	private final List<String> transactions = new ArrayList<>();

	public ScriptedDataSource rows(final String fragment, final Object[]... rows) {
		rules.add(0, new Rule(fragment, values -> List.of(rows)));
		return this;
	}

	public ScriptedDataSource fails(final String fragment, final String sqlState) {
		rules.add(0, new Rule(fragment, values -> new SQLException("falla simulada", sqlState)));
		return this;
	}

	public List<Call> calls() {
		return calls;
	}

	public List<String> calls(final String fragment) {
		return calls.stream().filter(call -> call.sql().contains(fragment)).map(call -> String.valueOf(call.values())).toList();
	}

	public List<String> transactions() {
		return transactions;
	}

	public DataSource dataSource() {
		return proxy(DataSource.class, (instance, method, arguments) -> switch (method.getName()) {
			case "getConnection" -> connection();
			case "toString" -> "ScriptedDataSource";
			default -> null;
		});
	}

	private Connection connection() {
		final boolean[] autoCommit = { true };
		return proxy(Connection.class, (instance, method, arguments) -> switch (method.getName()) {
			case "prepareStatement" -> statement((String) arguments[0]);
			case "setAutoCommit" -> {
				autoCommit[0] = (Boolean) arguments[0];
				yield null;
			}
			case "getAutoCommit" -> autoCommit[0];
			case "commit", "rollback" -> {
				transactions.add(method.getName());
				yield null;
			}
			case "isClosed" -> false;
			default -> null;
		});
	}

	private PreparedStatement statement(final String sql) {
		final List<Object> values = new ArrayList<>();
		return proxy(PreparedStatement.class, (instance, method, arguments) -> {
			final String name = method.getName();
			if (name.startsWith("set") && arguments != null && arguments.length >= 2) {
				final int position = (Integer) arguments[0];
				while (values.size() < position) {
					values.add(null);
				}
				values.set(position - 1, arguments[1]);
				return null;
			}
			return switch (name) {
				case "executeQuery" -> resultOf(answer(sql, values));
				case "executeUpdate" -> {
					answer(sql, values);
					yield 1;
				}
				case "execute" -> {
					answer(sql, values);
					yield true;
				}
				default -> null;
			};
		});
	}

	@SuppressWarnings("unchecked")
	private List<Object[]> answer(final String sql, final List<Object> values) throws SQLException {
		calls.add(new Call(sql, List.copyOf(values.stream().map(String::valueOf).toList())));
		for (final Rule rule : rules) {
			if (sql.contains(rule.fragment())) {
				final Object answer = rule.answer().apply(values);
				if (answer instanceof final SQLException exception) {
					throw exception;
				}
				return (List<Object[]>) answer;
			}
		}
		return List.of();
	}

	private static ResultSet resultOf(final List<Object[]> rows) {
		final int[] index = { -1 };
		return proxy(ResultSet.class, (instance, method, arguments) -> switch (method.getName()) {
			case "next" -> ++index[0] < rows.size();
			case "getObject", "getString" -> rows.get(index[0])[(Integer) arguments[0] - 1];
			case "getInt" -> ((Number) rows.get(index[0])[(Integer) arguments[0] - 1]).intValue();
			case "getLong" -> ((Number) rows.get(index[0])[(Integer) arguments[0] - 1]).longValue();
			default -> null;
		});
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(ScriptedDataSource.class.getClassLoader(), new Class<?>[] { type }, handler);
	}

}
