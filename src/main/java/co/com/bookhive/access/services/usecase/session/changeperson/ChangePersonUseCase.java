package co.com.bookhive.access.services.usecase.session.changeperson;

import co.com.bookhive.access.domain.session.changeperson.PersonChangeDomain;

public interface ChangePersonUseCase {

	void execute(PersonChangeDomain change);

}
