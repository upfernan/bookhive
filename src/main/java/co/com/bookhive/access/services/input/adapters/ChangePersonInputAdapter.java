package co.com.bookhive.access.services.input.adapters;

import java.util.Arrays;
import co.com.bookhive.access.domain.session.changeperson.PersonChangeDomain;
import co.com.bookhive.access.services.input.dto.request.ChangePersonRequestDTO;
import co.com.bookhive.access.services.input.ports.ChangePersonInputPort;
import co.com.bookhive.access.services.usecase.session.changeperson.ChangePersonUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class ChangePersonInputAdapter implements ChangePersonInputPort {

	private final ChangePersonUseCase useCase;

	public ChangePersonInputAdapter(final ChangePersonUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public void execute(final ChangePersonRequestDTO request) {
		final ChangePersonRequestDTO safe = ObjectHelper.getDefaultValue(request, new ChangePersonRequestDTO(null, null, null));
		final PersonChangeDomain.Action action = Arrays.stream(PersonChangeDomain.Action.values())
				.filter(value -> value.name().equals(safe.action()))
				.findFirst()
				.orElse(PersonChangeDomain.Action.DEACTIVATE);
		useCase.execute(new PersonChangeDomain(safe.person(), safe.schema(), action));
	}

}
