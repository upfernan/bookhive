package co.com.bookhive.circulation.infrastructure.input.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.com.bookhive.circulation.services.input.dto.request.RemindDueLoansRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.RemindDueLoansResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ScheduledTaskResponseDTO;
import co.com.bookhive.circulation.services.input.ports.RemindDueLoansInputPort;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.shared.infrastructure.input.messaging.PubSubPushMessage;

class LoanTasksControllerTest {

	private final List<String> calls = new ArrayList<>();

	private final RemindDueLoansInputPort remind = request -> {
		calls.add(TenantContext.get() + " " + request.dueOn());
		return new RemindDueLoansResponseDTO(2);
	};

	@AfterEach
	void clean() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("Cloud Scheduler reparte los recordatorios por institución")
	void schedulesReminders() {
		final LoanTasksController controller = new LoanTasksController(
				() -> new ScheduledTaskResponseDTO(List.of("uco", "unal")), remind, 2);

		assertEquals(List.of("uco", "unal"), controller.scheduleDueReminders().getBody().institutions());
	}

	@Test
	@DisplayName("la tarea de una institución recuerda los préstamos que vencen en dos días, dentro de su esquema")
	void runsReminderInsideInstitution() {
		final LoanTasksController controller = new LoanTasksController(() -> new ScheduledTaskResponseDTO(List.of()), remind, 2);

		final var response = controller.runInstitutionTask(push("recordar-vencimientos", "uco"));

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		final LocalDate expected = DateHelper.todayInUtc().plusDays(2);
		assertEquals(List.of("uco " + expected), calls);
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("descarta mensajes de otra tarea o sin institución válida sin reintentar")
	void discardsUnknownTasks() {
		final LoanTasksController controller = new LoanTasksController(() -> new ScheduledTaskResponseDTO(List.of()), remind, -1);

		assertEquals(HttpStatus.NO_CONTENT, controller.runInstitutionTask(push("otra", "uco")).getStatusCode());
		assertEquals(HttpStatus.NO_CONTENT,
				controller.runInstitutionTask(push(RemindDueLoansRequestDTO.TASK, "UCO; DROP")).getStatusCode());
		assertTrue(calls.isEmpty());
	}

	private static PubSubPushMessage push(final String task, final String institution) {
		return new PubSubPushMessage(new PubSubPushMessage.Message("e30=",
				Map.of("tarea", task, "institucion", institution), "7"), "s");
	}

}
