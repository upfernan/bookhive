package co.com.bookhive.circulation.services.usecase.account.changeuserstatus;

import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;

public interface ChangeUserStatusUseCase {

	UserLifecycleDomains.Account execute(UserLifecycleDomains.Request request);

}
