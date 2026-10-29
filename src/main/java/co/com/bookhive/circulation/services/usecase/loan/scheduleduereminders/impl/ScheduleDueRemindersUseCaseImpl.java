package co.com.bookhive.circulation.services.usecase.loan.scheduleduereminders.impl;

import java.util.List;
import co.com.bookhive.circulation.domain.loan.scheduleduereminders.ScheduleDueRemindersDomain;
import co.com.bookhive.circulation.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.circulation.services.output.ports.InstitutionTaskPort;
import co.com.bookhive.circulation.services.usecase.loan.scheduleduereminders.ScheduleDueRemindersUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ScheduleDueRemindersUseCaseImpl implements ScheduleDueRemindersUseCase {

	private final InstitutionDirectoryPort institutionDirectory;
	private final InstitutionTaskPort institutionTasks;

	public ScheduleDueRemindersUseCaseImpl(
			final InstitutionDirectoryPort institutionDirectory,
			final InstitutionTaskPort institutionTasks) {

		super();
		this.institutionDirectory = requireDependency(institutionDirectory, "el directorio de instituciones");
		this.institutionTasks = requireDependency(institutionTasks, "la cola de tareas por institución");
	}

	@Override
	public ScheduleDueRemindersDomain execute() {
		final List<String> institutions = institutionDirectory.activeSchemas();
		institutions.forEach(institution -> {
			institutionTasks.schedule(ScheduleDueRemindersDomain.TASK, institution);
			institutionTasks.schedule(ScheduleDueRemindersDomain.RESERVATIONS_TASK, institution);
		});
		return new ScheduleDueRemindersDomain(institutions);
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"El caso de uso ScheduleDueReminders se creó sin " + name + ".");
		}
		return dependency;
	}

}
