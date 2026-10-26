package co.com.bookhive.circulation.services.usecase.location.listbranches.impl;

import java.util.List;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.listbranches.ListBranchesUseCase;

public final class ListBranchesUseCaseImpl implements ListBranchesUseCase {

	private final LocationRepository locations;

	public ListBranchesUseCaseImpl(final LocationRepository locations) {
		super();
		this.locations = FindBookCopyLocationUseCaseImpl.requireDependency(locations, "el repositorio de ubicaciones");
	}

	@Override
	public List<LocationDomains.Branch> execute() {
		return locations.findBranches().stream().map(branch -> new LocationDomains.Branch(branch.id(), branch.name())).toList();
	}

}
