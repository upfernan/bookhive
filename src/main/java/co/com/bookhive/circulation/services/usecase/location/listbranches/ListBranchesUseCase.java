package co.com.bookhive.circulation.services.usecase.location.listbranches;

import java.util.List;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface ListBranchesUseCase {

	List<LocationDomains.Branch> execute();

}
