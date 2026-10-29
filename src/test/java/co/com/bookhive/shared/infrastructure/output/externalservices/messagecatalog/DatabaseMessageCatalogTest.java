package co.com.bookhive.shared.infrastructure.output.externalservices.messagecatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;

class DatabaseMessageCatalogTest {

	private static final String KEY = "book-copy.not-available";
	private static final String TABLE_TEXT = "El ejemplar no está disponible.";
	private static final String SHIPPED_TEXT = "Texto que viene con la aplicación.";
	private static final long KEEP_FIVE_MINUTES = 300;
	private static final long ALWAYS_RELOAD = -1;

	private final AtomicInteger reads = new AtomicInteger();

	@Test
	@DisplayName("toma el texto de la tabla de mensajes")
	void readsTextFromTable() {
		final DatabaseMessageCatalog catalog = catalog(table(Map.of(KEY, TABLE_TEXT)), shipped(), KEEP_FIVE_MINUTES);

		assertEquals(TABLE_TEXT, catalog.textOf("  " + KEY + "  "));
	}

	@Test
	@DisplayName("si la tabla no tiene la clave usa el texto que viene con la aplicación")
	void fallsBackToShippedText() {
		final DatabaseMessageCatalog catalog = catalog(table(Map.of()), shipped(), KEEP_FIVE_MINUTES);

		assertEquals(SHIPPED_TEXT, catalog.textOf(KEY));
	}

	@Test
	@DisplayName("si la clave no existe en ningún lado responde con el mensaje general")
	void unknownKeyGetsLastResortText() {
		final DatabaseMessageCatalog catalog = catalog(table(Map.of()), shipped(), KEEP_FIVE_MINUTES);

		assertTrue(catalog.textOf("clave.que.no.existe").startsWith("Ocurrió un error inesperado"));
	}

	@Test
	@DisplayName("sin clave responde con el mensaje general sin ir a la base de datos")
	void blankKeyGetsLastResortText() {
		final DatabaseMessageCatalog catalog = catalog(table(Map.of(KEY, TABLE_TEXT)), shipped(), KEEP_FIVE_MINUTES);

		assertTrue(catalog.textOf("   ").startsWith("Ocurrió un error inesperado"));
		assertEquals(0, reads.get());
	}

	@Test
	@DisplayName("guarda el catálogo en memoria y no lo vuelve a leer mientras esté vigente")
	void keepsCatalogInMemory() {
		final DatabaseMessageCatalog catalog = catalog(table(Map.of(KEY, TABLE_TEXT)), shipped(), KEEP_FIVE_MINUTES);

		catalog.textOf(KEY);
		catalog.textOf(KEY);

		assertEquals(1, reads.get());
	}

	@Test
	@DisplayName("vuelve a leer la tabla cuando el catálogo en memoria vence")
	void reloadsExpiredCatalog() {
		final DatabaseMessageCatalog catalog = catalog(table(Map.of(KEY, TABLE_TEXT)), shipped(), ALWAYS_RELOAD);

		catalog.textOf(KEY);
		catalog.textOf(KEY);

		assertEquals(2, reads.get());
	}

	@Test
	@DisplayName("si la base de datos falla usa el texto que viene con la aplicación")
	void survivesDatabaseFailure() {
		final DatabaseMessageCatalog catalog = catalog(failingTable(), shipped(), KEEP_FIVE_MINUTES);

		assertEquals(SHIPPED_TEXT, catalog.textOf(KEY));
	}

	@Test
	@DisplayName("si tampoco se pueden leer los textos de la aplicación responde con el mensaje general")
	void survivesBrokenShippedMessages() {
		final MessageSource broken = proxy(MessageSource.class, (instance, method, arguments) -> {
			throw new IllegalStateException("Archivo de mensajes dañado.");
		});
		final DatabaseMessageCatalog catalog = catalog(table(Map.of()), broken, KEEP_FIVE_MINUTES);

		assertTrue(catalog.textOf(KEY).startsWith("Ocurrió un error inesperado"));
	}

	private static DatabaseMessageCatalog catalog(
			final DataSource dataSource, final MessageSource shippedMessages, final long refreshSeconds) {

		return new DatabaseMessageCatalog(dataSource, shippedMessages, refreshSeconds);
	}

	private static MessageSource shipped() {
		final StaticMessageSource messages = new StaticMessageSource();
		messages.addMessage(KEY, Locale.getDefault(), SHIPPED_TEXT);
		return messages;
	}

	private DataSource table(final Map<String, String> rows) {
		return proxy(DataSource.class, (instance, method, arguments) -> {
			if (!"getConnection".equals(method.getName())) {
				return null;
			}
			reads.incrementAndGet();
			return connectionReading(List.copyOf(rows.entrySet()));
		});
	}

	private DataSource failingTable() {
		return proxy(DataSource.class, (instance, method, arguments) -> {
			reads.incrementAndGet();
			throw new SQLException("La base de datos no responde.");
		});
	}

	private static Connection connectionReading(final List<Map.Entry<String, String>> rows) {
		final Iterator<Map.Entry<String, String>> cursor = rows.iterator();
		final Map.Entry<String, String>[] current = newRow();
		final ResultSet result = proxy(ResultSet.class, (instance, method, arguments) -> switch (method.getName()) {
			case "next" -> advance(cursor, current);
			case "getString" -> "codigo".equals(arguments[0]) ? current[0].getKey() : current[0].getValue();
			default -> null;
		});
		final PreparedStatement statement = proxy(PreparedStatement.class,
				(instance, method, arguments) -> "executeQuery".equals(method.getName()) ? result : null);
		return proxy(Connection.class,
				(instance, method, arguments) -> "prepareStatement".equals(method.getName()) ? statement : null);
	}

	@SuppressWarnings("unchecked")
	private static Map.Entry<String, String>[] newRow() {
		return new Map.Entry[1];
	}

	private static boolean advance(
			final Iterator<Map.Entry<String, String>> cursor, final Map.Entry<String, String>[] current) {

		if (!cursor.hasNext()) {
			return false;
		}
		current[0] = cursor.next();
		return true;
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(
				DatabaseMessageCatalogTest.class.getClassLoader(), new Class<?>[] { type }, handler));
	}

}
