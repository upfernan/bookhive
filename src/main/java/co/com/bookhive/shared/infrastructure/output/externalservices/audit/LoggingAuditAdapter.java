package co.com.bookhive.shared.infrastructure.output.externalservices.audit;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Component
public class LoggingAuditAdapter implements AuditPort {

	static final String AUDIT_LOGGER = "bookhive.auditoria";

	private static final Logger logger = LoggerFactory.getLogger(AUDIT_LOGGER);

	@Override
	public void record(final String action, final UUID institution, final UUID person, final String detail) {

		final String event = describe(action, institution, person, detail);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

				@Override
				public void afterCommit() {
					logger.info(event);
				}

			});
			return;
		}

		logger.info(event);
	}

	static String describe(final String action, final UUID institution, final UUID person, final String detail) {
		return "AUDITORIA accion=" + TextHelper.getDefaultValue(action)
				+ " institucion=" + UUIDHelper.getDefaultValue(institution)
				+ " persona=" + UUIDHelper.getDefaultValue(person)
				+ " detalle=" + TextHelper.getDefaultValue(detail).replaceAll("\\s+", "_");
	}

}
