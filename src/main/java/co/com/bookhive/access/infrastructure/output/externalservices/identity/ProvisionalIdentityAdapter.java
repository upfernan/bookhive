package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import co.com.bookhive.access.services.output.ports.SessionIdentityPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "bookhive.session.provisional.enabled", havingValue = "true")
public class ProvisionalIdentityAdapter implements SessionIdentityPort {

	private static final Logger logger = LoggerFactory.getLogger(ProvisionalIdentityAdapter.class);

	private final String identity;

	public ProvisionalIdentityAdapter(
			@Value("${bookhive.session.provisional.identity:}") final String identity) {

		super();
		this.identity = TextHelper.getDefaultValue(identity).trim();
	}

	@PostConstruct
	void warnAboutProvisionalIdentity() {
		logger.warn("Identidad de sesion PROVISIONAL: la persona sale de la configuracion,"
				+ " no del pase (ADR-026, ADR-028).");
	}

	@Override
	public String getIdentity() {

		if (TextHelper.isDefaultValue(identity)) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_NOT_RESOLVED,
					"Falta la propiedad bookhive.session.provisional.identity, que suple la identidad"
							+ " mientras no se use el proveedor de identidad (ADR-028).");
		}

		return identity;
	}

}
