package co.com.bookhive.circulation.infrastructure.input.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import co.com.bookhive.circulation.services.input.dto.request.ExpireReservationsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.RemindDueLoansRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ExpiredReservationsResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.RemindDueLoansResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ScheduledTaskResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ExpireReservationsInputPort;
import co.com.bookhive.circulation.services.input.ports.RemindDueLoansInputPort;
import co.com.bookhive.circulation.services.input.ports.ScheduleDueRemindersInputPort;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.shared.infrastructure.input.messaging.PubSubPushMessage;

class LoanTasksControllerTest {

	private final List<String> calls = new ArrayList<>();
	private final FakeTransactions transactions = new FakeTransactions();

	private final RemindDueLoansInputPort remind = request -> {
		calls.add(TenantContext.get() + " " + request.daysBefore());
		return new RemindDueLoansResponseDTO(2);
	};

	private final ExpireReservationsInputPort expire = request -> {
		calls.add("vencer en " + TenantContext.get());
		return new ExpiredReservationsResponseDTO(3, 1);
	};

	@AfterEach
	void clean() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("Cloud Scheduler reparte los recordatorios por institución")
	void schedulesReminders() {
		final LoanTasksController controller = controller(
				() -> new ScheduledTaskResponseDTO(List.of("uco", "unal")), expire, 2);

		assertEquals(List.of("uco", "unal"), controller.scheduleDueReminders().getBody().institutions());
	}

	@Test
	@DisplayName("la tarea de una institución pide los recordatorios a dos días, dentro de su esquema")
	void runsReminderInsideInstitution() {
		final LoanTasksController controller = controller(() -> new ScheduledTaskResponseDTO(List.of()), expire, 2);

		final var response = controller.runInstitutionTask(push("recordar-vencimientos", "uco"));

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertEquals(List.of("uco 2"), calls);
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("vence las reservas dentro del esquema y abre la transacción después de fijar la institución")
	void expiresReservationsInsideInstitutionTransaction() {
		final LoanTasksController controller = controller(() -> new ScheduledTaskResponseDTO(List.of()), expire, 2);

		final var response = controller.runInstitutionTask(push(ExpireReservationsRequestDTO.TASK, "uco"));

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertEquals(List.of("vencer en uco"), calls);
		assertEquals(List.of("abrir en uco", "confirmar"), transactions.events);
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("si el vencimiento no devuelve resultado lo cuenta como cero sin fallar")
	void toleratesEmptyExpiryResult() {
		final LoanTasksController controller = controller(
				() -> new ScheduledTaskResponseDTO(List.of()), request -> null, 2);

		assertEquals(HttpStatus.NO_CONTENT,
				controller.runInstitutionTask(push(ExpireReservationsRequestDTO.TASK, "uco")).getStatusCode());
		assertEquals(List.of("abrir en uco", "confirmar"), transactions.events);
	}

	@Test
	@DisplayName("descarta mensajes de otra tarea o sin institución válida sin reintentar")
	void discardsUnknownTasks() {
		final LoanTasksController controller = controller(() -> new ScheduledTaskResponseDTO(List.of()), expire, -1);

		assertEquals(HttpStatus.NO_CONTENT, controller.runInstitutionTask(push("otra", "uco")).getStatusCode());
		assertEquals(HttpStatus.NO_CONTENT,
				controller.runInstitutionTask(push(RemindDueLoansRequestDTO.TASK, "UCO; DROP")).getStatusCode());
		assertEquals(HttpStatus.NO_CONTENT,
				controller.runInstitutionTask(push(ExpireReservationsRequestDTO.TASK, "UCO; DROP")).getStatusCode());
		assertTrue(calls.isEmpty());
		assertTrue(transactions.events.isEmpty());
	}

	private LoanTasksController controller(
			final ScheduleDueRemindersInputPort schedule,
			final ExpireReservationsInputPort expireReservations,
			final int daysBefore) {

		return new LoanTasksController(schedule, remind, expireReservations, transactions, daysBefore);
	}

	private static PubSubPushMessage push(final String task, final String institution) {
		return new PubSubPushMessage(new PubSubPushMessage.Message("e30=",
				Map.of("tarea", task, "institucion", institution), "7"), "s");
	}

	private static final class FakeTransactions implements PlatformTransactionManager {

		private final List<String> events = new ArrayList<>();

		@Override
		public TransactionStatus getTransaction(final TransactionDefinition definition) {
			events.add("abrir en " + TenantContext.get());
			return new SimpleTransactionStatus();
		}

		@Override
		public void commit(final TransactionStatus status) {
			events.add("confirmar");
		}

		@Override
		public void rollback(final TransactionStatus status) {
			events.add("deshacer");
		}

	}

}
