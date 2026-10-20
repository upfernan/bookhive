package co.com.bookhive.gateway.infrastructure.output.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoggingAuditAdapterTest {

	@Test
	@DisplayName("el evento lleva acción, cuenta, origen y detalle sin espacios")
	void describesEvent() {
		assertEquals("AUDITORIA accion=INTENTO_FALLIDO cuenta=uid-1 origen=191.91.85.226 detalle=accion_prestar_estado_403",
				LoggingAuditAdapter.describe("INTENTO_FALLIDO", "uid-1", "191.91.85.226", "accion prestar  estado 403"));
	}

	@Test
	@DisplayName("los campos vacíos quedan con un guion")
	void emptyFieldsUseDash() {
		assertEquals("AUDITORIA accion=- cuenta=- origen=- detalle=-",
				LoggingAuditAdapter.describe(null, " ", "", null));
	}

	@Test
	@DisplayName("registrar no falla")
	void recordDoesNotFail() {
		new LoggingAuditAdapter().register("SOLICITUD_FRENADA", "uid-1", "10.0.0.1", "espera 60s");
		assertEquals("bookhive.auditoria", LoggingAuditAdapter.AUDIT_LOGGER);
	}

}
