package co.com.bookhive.gateway.infrastructure.output.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;
import co.com.bookhive.gateway.services.output.ports.AuditPort;

@Component
public class LoggingAuditAdapter implements AuditPort {

	static final String AUDIT_LOGGER = "bookhive.auditoria";

	private static final String EMPTY = "-";
	private static final Logger logger = LoggerFactory.getLogger(AUDIT_LOGGER);

	@Override
	public void record(final String action, final String account, final String origin, final String detail) {
		logger.info(describe(action, account, origin, detail));
	}

	static String describe(final String action, final String account, final String origin, final String detail) {
		return "AUDITORIA accion=" + clean(action)
				+ " cuenta=" + clean(account)
				+ " origen=" + clean(origin)
				+ " detalle=" + clean(detail);
	}

	private static String clean(final String value) {
		return TextHelper.getDefaultValue(TextHelper.getDefaultValue(value).replaceAll("\\s+", "_"), EMPTY);
	}

}
