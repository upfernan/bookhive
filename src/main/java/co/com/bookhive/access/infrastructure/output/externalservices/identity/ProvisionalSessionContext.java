package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import co.com.bookhive.access.services.output.ports.SessionContextPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "bookhive.session.provisional.enabled", havingValue = "true")
public class ProvisionalSessionContext implements SessionContextPort {

	private static final Logger logger = LoggerFactory.getLogger(ProvisionalSessionContext.class);

	private final String institution;
	private final String librarian;

	public ProvisionalSessionContext(
			@Value("${bookhive.session.provisional.institution:}") final String institution,
			@Value("${bookhive.session.provisional.librarian:}") final String librarian) {

		super();
		this.institution = TextHelper.getDefaultValue(institution);
		this.librarian = TextHelper.getDefaultValue(librarian);
	}

	@PostConstruct
	void warnAboutProvisionalContext() {
		logger.warn("Contexto de sesion PROVISIONAL: la institucion y el bibliotecario salen de"
				+ " la configuracion, no del token. Falta conectar el proveedor de identidad"
				+ " (ADR-026, ADR-028).");
	}

	@Override
	public UUID getInstitution() {
		return readConfigured(institution, "la institución",
				"bookhive.session.provisional.institution");
	}

	@Override
	public UUID getLibrarian() {
		return readConfigured(librarian, "el bibliotecario",
				"bookhive.session.provisional.librarian");
	}

	private static UUID readConfigured(
			final String value, final String name, final String property) {

		if (TextHelper.isDefaultValue(value)) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_NOT_RESOLVED,
					"Falta la propiedad " + property + ", que suple " + name
							+ " mientras no exista el proveedor de identidad (ADR-028).");
		}

		try {
			return UUID.fromString(value.trim());
		} catch (final IllegalArgumentException excepcion) {
			throw BookHiveException.createUnexpected(
					excepcion,
					MessageKey.SESSION_NOT_RESOLVED,
					"La propiedad " + property + " no tiene forma de identificador: " + value);
		}
	}

}
