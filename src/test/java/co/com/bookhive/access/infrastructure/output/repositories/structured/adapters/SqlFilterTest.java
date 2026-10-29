package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SqlFilterTest {

	@Test
	@DisplayName("sin condiciones deja la consulta tal cual")
	void keepsQueryWithoutConditions() {
		assertEquals("SELECT 1", new SqlFilter("SELECT 1").query());
	}

	@Test
	@DisplayName("une las condiciones con WHERE y AND y pone los valores en orden")
	void joinsConditionsAndBindsValuesInOrder() throws Exception {
		final SqlFilter filter = new SqlFilter("SELECT id FROM persona");
		filter.add("identidad = ?", "uid");
		filter.add("estado = ?", "ACTIVA");
		final List<String> bound = new ArrayList<>();

		filter.bind(statement(bound));

		assertEquals("SELECT id FROM persona WHERE identidad = ? AND estado = ?", filter.query());
		assertEquals(List.of("1=uid", "2=ACTIVA"), bound);
	}

	private static PreparedStatement statement(final List<String> bound) {
		return (PreparedStatement) Proxy.newProxyInstance(
				SqlFilterTest.class.getClassLoader(), new Class<?>[] { PreparedStatement.class },
				(instance, method, arguments) -> {
					if ("setObject".equals(method.getName())) {
						bound.add(arguments[0] + "=" + arguments[1]);
					}
					return null;
				});
	}

}
