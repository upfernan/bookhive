package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.services.input.dto.response.ScheduledTaskResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ScheduleDueRemindersInputPort;
import co.com.bookhive.circulation.services.usecase.loan.scheduleduereminders.ScheduleDueRemindersUseCase;

public final class ScheduleDueRemindersInputAdapter implements ScheduleDueRemindersInputPort {

	private final ScheduleDueRemindersUseCase useCase;

	public ScheduleDueRemindersInputAdapter(final ScheduleDueRemindersUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ScheduledTaskResponseDTO execute() {
		return new ScheduledTaskResponseDTO(useCase.execute().institutions());
	}

}
