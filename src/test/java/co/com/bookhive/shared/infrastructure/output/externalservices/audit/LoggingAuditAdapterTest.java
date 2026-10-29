package co.com.bookhive.shared.infrastructure.output.externalservices.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class LoggingAuditAdapterTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID PERSON = UUIDHelper.generateNew();

	private final LoggingAuditAdapter adapter = new LoggingAuditAdapter();

	@Test
	@DisplayName("el evento lleva la acción, la institución y el número de la persona, nunca su nombre")
	void describesEvent() {
		assertEquals(
				"AUDITORIA accion=" + AuditPort.LOAN_REGISTERED + " institucion=" + INSTITUTION
						+ " persona=" + PERSON + " detalle=prestamo-1",
				LoggingAuditAdapter.describe(AuditPort.LOAN_REGISTERED, INSTITUTION, PERSON, "prestamo-1"));
	}

	@Test
	@DisplayName("los datos que faltan quedan con su valor por defecto y el detalle no puede partir la línea")
	void keepsEventOnOneLine() {
		final String event = LoggingAuditAdapter.describe(null, null, null, "uno\ndos tres");

		assertEquals("AUDITORIA accion= institucion=" + UUIDHelper.getDefaultValue()
				+ " persona=" + UUIDHelper.getDefaultValue() + " detalle=uno_dos_tres", event);
		assertFalse(event.contains("\n"));
	}

	@Test
	@DisplayName("dentro de una transacción el evento se escribe solo cuando el cambio se confirma")
	void waitsForCommit() {
		TransactionSynchronizationManager.initSynchronization();

		try {
			adapter.register(AuditPort.LOAN_REGISTERED, INSTITUTION, PERSON, "prestamo-1");

			final List<TransactionSynchronization> pending = TransactionSynchronizationManager.getSynchronizations();
			assertEquals(1, pending.size());
			assertDoesNotThrow(() -> pending.get(0).afterCommit());
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	@DisplayName("fuera de una transacción el evento se escribe de inmediato")
	void writesImmediatelyWithoutTransaction() {
		assertDoesNotThrow(() -> adapter.register(AuditPort.PERMISSION_DENIED, INSTITUTION, PERSON, "loan.lend"));
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

}
