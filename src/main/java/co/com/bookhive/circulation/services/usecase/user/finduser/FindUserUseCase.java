package co.com.bookhive.circulation.services.usecase.user.finduser;

import co.com.bookhive.circulation.domain.user.finduser.FindUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface FindUserUseCase extends UseCaseWithReturn<FindUserDomain, FoundUserDomain> {
}
