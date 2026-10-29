package co.com.bookhive.circulation.services.usecase.loan.scheduleduereminders.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.loan.scheduleduereminders.ScheduleDueRemindersDomain;
import co.com.bookhive.circulation.services.input.adapters.ScheduleDueRemindersInputAdapter;
import co.com.bookhive.circulation.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.circulation.services.output.ports.InstitutionTaskPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class ScheduleDueRemindersUseCaseImplTest {

	@Test
	@DisplayName("deja en la cola los recordatorios y el vencimiento de reservas de cada institución activa")
	void schedulesDailyTasksPerInstitution() {
		final List<String> scheduled = new ArrayList<>();
		final InstitutionTaskPort tasks = (task, institution) -> scheduled.add(task + "@" + institution);

		final ScheduleDueRemindersDomain result =
				new ScheduleDueRemindersUseCaseImpl(() -> List.of("uco", "unal"), tasks).execute();

		assertEquals(List.of("uco", "unal"), result.institutions());
		assertEquals(List.of(
				ScheduleDueRemindersDomain.TASK + "@uco",
				ScheduleDueRemindersDomain.RESERVATIONS_TASK + "@uco",
				ScheduleDueRemindersDomain.TASK + "@unal",
				ScheduleDueRemindersDomain.RESERVATIONS_TASK + "@unal"),
				scheduled);
		assertEquals(List.of("uco"), new ScheduleDueRemindersInputAdapter(
				() -> new ScheduleDueRemindersDomain(List.of("uco"))).execute().institutions());
		assertEquals(List.of(), new ScheduleDueRemindersDomain(null).institutions());
	}

	@Test
	@DisplayName("no se crea sin el directorio ni la cola de tareas")
	void requiresDependencies() {
		final InstitutionDirectoryPort directory = List::of;
		final InstitutionTaskPort tasks = (task, institution) -> { };

		assertThrows(BookHiveException.class, () -> new ScheduleDueRemindersUseCaseImpl(null, tasks));
		assertThrows(BookHiveException.class, () -> new ScheduleDueRemindersUseCaseImpl(directory, null));
	}

}
