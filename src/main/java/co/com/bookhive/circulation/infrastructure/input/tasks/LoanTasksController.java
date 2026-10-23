package co.com.bookhive.circulation.infrastructure.input.tasks;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.circulation.services.input.dto.request.RemindDueLoansRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.RemindDueLoansResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ScheduledTaskResponseDTO;
import co.com.bookhive.circulation.services.input.ports.RemindDueLoansInputPort;
import co.com.bookhive.circulation.services.input.ports.ScheduleDueRemindersInputPort;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.shared.infrastructure.input.messaging.PubSubPushMessage;

@RestController
@RequestMapping("/api/v1/tareas")
public class LoanTasksController {

	private static final Logger logger = LoggerFactory.getLogger(LoanTasksController.class);
	private static final Pattern INSTITUTION = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

	private final ScheduleDueRemindersInputPort scheduleDueReminders;
	private final RemindDueLoansInputPort remindDueLoans;
	private final int daysBefore;

	public LoanTasksController(
			final ScheduleDueRemindersInputPort scheduleDueReminders,
			final RemindDueLoansInputPort remindDueLoans,
			@Value("${bookhive.reminders.days-before:2}") final int daysBefore) {

		super();
		this.scheduleDueReminders = scheduleDueReminders;
		this.remindDueLoans = remindDueLoans;
		this.daysBefore = Math.max(daysBefore, 0);
	}

	@PostMapping("/recordar-vencimientos")
	public ResponseEntity<ScheduledTaskResponseDTO> scheduleDueReminders() {
		logger.info("Cloud Scheduler pidió los recordatorios de vencimiento.");
		final ScheduledTaskResponseDTO response = scheduleDueReminders.execute();
		logger.info("Recordatorios en cola para {} instituciones.", response.institutions().size());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/prestamos")
	public ResponseEntity<Void> runInstitutionTask(@RequestBody final PubSubPushMessage push) {
		final String task = push.attribute("tarea");
		final String institution = push.attribute("institucion");
		final String messageId = push.messageId();

		if (!RemindDueLoansRequestDTO.TASK.equals(task) || !INSTITUTION.matcher(institution).matches()) {
			logger.warn("Se descartó la tarea {} del mensaje {}: no es de préstamos o no trae una institución válida.",
					task, messageId);
			return ResponseEntity.noContent().build();
		}

		TenantContext.set(institution);
		try {
			final RemindDueLoansResponseDTO response = remindDueLoans.execute(
					new RemindDueLoansRequestDTO(DateHelper.todayInUtc().plusDays(daysBefore)));
			logger.info("Recordatorios pedidos en {}: {}.", institution, response.reminded());
		} finally {
			TenantContext.clear();
		}
		return ResponseEntity.noContent().build();
	}

}
