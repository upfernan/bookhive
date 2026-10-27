package co.com.bookhive.circulation.services.usecase.account.purgeexpiredusers;

import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;

public interface PurgeExpiredUsersUseCase {

	UserLifecycleDomains.PurgeRun execute(String institution);

}
