package co.com.bookhive.circulation.services.usecase.location.createbranch;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface CreateBranchUseCase {

	LocationDomains.Branch execute(LocationDomains.Branch branch);

}
