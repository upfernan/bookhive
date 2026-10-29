package co.com.bookhive.shared.infrastructure.output.externalservices.messagecatalog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.shared.services.output.ports.MessageCatalogPort;

@Component
public class DatabaseMessageCatalog implements MessageCatalogPort {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseMessageCatalog.class);

	private static final String LAST_RESORT_TEXT =
			"Ocurrió un error inesperado. Por favor inténtelo de nuevo y si el problema persiste"
					+ " contacte a un administrador.";

	private static final String READ_CATALOG = "SELECT codigo, texto FROM compartido.mensaje";

	private final DataSource dataSource;
	private final MessageSource shippedMessages;
	private final Duration refreshEvery;

	private volatile CachedCatalog cached = new CachedCatalog(Map.of(), Instant.EPOCH);

	public DatabaseMessageCatalog(
			final DataSource dataSource,
			final MessageSource shippedMessages,
			@Value("${bookhive.messages.refresh-seconds:300}") final long refreshSeconds) {

		super();
		this.dataSource = dataSource;
		this.shippedMessages = shippedMessages;
		this.refreshEvery = Duration.ofSeconds(refreshSeconds);
	}

	@Override
	public String textOf(final String messageKey) {

		final String key = TextHelper.getDefaultValue(messageKey);

		if (key.isEmpty()) {
			return LAST_RESORT_TEXT;
		}

		final String fromTable = catalog().get(key);

		if (!TextHelper.isDefaultValue(fromTable)) {
			return fromTable;
		}

		return shippedText(key);
	}

	private String shippedText(final String messageKey) {

		try {
			return shippedMessages.getMessage(
					messageKey, null, LAST_RESORT_TEXT, Locale.getDefault());
		} catch (final RuntimeException exception) {
			logger.error("No se pudo leer el mensaje {} de los archivos de la aplicación.",
					messageKey, exception);
			return LAST_RESORT_TEXT;
		}
	}

	private Map<String, String> catalog() {

		final CachedCatalog current = cached;

		if (!current.isOlderThan(refreshEvery)) {
			return current.texts();
		}

		return reload(current);
	}

	private Map<String, String> reload(final CachedCatalog current) {

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(READ_CATALOG);
			 ResultSet result = statement.executeQuery()) {

			final Map<String, String> texts = new HashMap<>();

			while (result.next()) {
				texts.put(result.getString("codigo"), result.getString("texto"));
			}

			cached = new CachedCatalog(Map.copyOf(texts), Instant.now());

			return cached.texts();

		} catch (final RuntimeException | java.sql.SQLException exception) {
			logger.error("No se pudo leer el catálogo de mensajes de la base de datos;"
					+ " se usan los textos que vienen con la aplicación.", exception);

			cached = new CachedCatalog(current.texts(), Instant.now());

			return current.texts();
		}
	}

	private record CachedCatalog(Map<String, String> texts, Instant loadedAt) {

		private boolean isOlderThan(final Duration age) {
			return Duration.between(loadedAt, Instant.now()).compareTo(age) > 0;
		}

	}

}
