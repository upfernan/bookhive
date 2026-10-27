package co.com.bookhive.circulation.services.usecase.account.configureretention;

import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;

public interface ConfigureRetentionUseCase {

	UserLifecycleDomains.Retention execute(UserLifecycleDomains.Retention retention);

}
