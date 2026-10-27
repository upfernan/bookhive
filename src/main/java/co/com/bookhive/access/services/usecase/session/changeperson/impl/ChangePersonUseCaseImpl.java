package co.com.bookhive.access.services.usecase.session.changeperson.impl;

import co.com.bookhive.access.domain.session.changeperson.PersonChangeDomain;
import co.com.bookhive.access.services.output.ports.repositories.PersonStatusRepository;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.access.services.usecase.session.changeperson.ChangePersonUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class ChangePersonUseCaseImpl implements ChangePersonUseCase {

	private static final String INACTIVE = "INACTIVA";

	private final PersonStatusRepository people;

	public ChangePersonUseCaseImpl(final PersonStatusRepository people) {
		super();
		this.people = people;
	}

	@Override
	public void execute(final PersonChangeDomain input) {
		final PersonChangeDomain change = ObjectHelper.getDefaultValue(input, new PersonChangeDomain(null, null, null));
		change.validate();
		switch (change.action()) {
			case ANONYMIZE -> people.anonymize(change.person(), change.schema());
			case REACTIVATE -> people.changeStatus(change.person(), change.schema(), PersonEntity.STATUS_ACTIVE);
			case DEACTIVATE -> people.changeStatus(change.person(), change.schema(), INACTIVE);
		}
	}

}
