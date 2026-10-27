package co.com.bookhive.access.services.usecase.session.registerperson;

import co.com.bookhive.access.domain.session.registerperson.RegistrationDomain;

public interface RegisterPersonUseCase {

	RegistrationDomain.Registered execute(RegistrationDomain registration);

}
