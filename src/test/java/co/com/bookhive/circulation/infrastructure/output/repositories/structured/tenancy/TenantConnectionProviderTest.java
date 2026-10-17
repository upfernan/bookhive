package co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class TenantConnectionProviderTest {

	private static final String SCHEMA = "institucion_uco";

	private final List<String> events = new ArrayList<>();
	private final TenantConnectionProvider provider = new TenantConnectionProvider(FakeDataSource.recording(events));

	@Test
	@DisplayName("entrega una conexión sin esquema y la cierra al soltarla")
	void handlesAnyConnection() throws SQLException {
		final Connection connection = provider.getAnyConnection();

		provider.releaseAnyConnection(connection);

		assertEquals(List.of("close"), events);
	}

	@Test
	@DisplayName("apunta la conexión al esquema de la institución")
	void usesInstitutionSchema() throws SQLException {
		provider.getConnection(SCHEMA);

		assertEquals(List.of("SET search_path TO " + SCHEMA), events);
	}

	@Test
	@DisplayName("al soltar la conexión vuelve al esquema público y la cierra")
	void resetsSchemaOnRelease() throws SQLException {
		final Connection connection = provider.getConnection(SCHEMA);
		events.clear();

		provider.releaseConnection(SCHEMA, connection);

		assertEquals(List.of("SET search_path TO " + TenantContext.SCHEMA_WITHOUT_TENANT, "close"), events);
	}

	@Test
	@DisplayName("no deja usar un nombre de esquema que pueda dañar la consulta")
	void rejectsUnsafeSchema() {
		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> provider.getConnection("uco; DROP SCHEMA compartido"));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertTrue(events.isEmpty());
	}

	@Test
	@DisplayName("le dice a Hibernate cómo maneja las conexiones")
	void describesConnectionHandling() {
		assertFalse(provider.supportsAggressiveRelease());
		assertTrue(provider.handlesConnectionSchema());
		assertFalse(provider.isUnwrappableAs(DataSource.class));
		assertNull(provider.unwrap(DataSource.class));
	}

}
