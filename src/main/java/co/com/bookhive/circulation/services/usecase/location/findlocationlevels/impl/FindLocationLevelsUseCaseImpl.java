package co.com.bookhive.circulation.services.usecase.location.findlocationlevels.impl;

import java.util.List;
import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.findlocationlevels.FindLocationLevelsUseCase;

public final class FindLocationLevelsUseCaseImpl implements FindLocationLevelsUseCase {

	private final LocationRepository locations;

	public FindLocationLevelsUseCaseImpl(final LocationRepository locations) {
		super();
		this.locations = FindBookCopyLocationUseCaseImpl.requireDependency(locations, "el repositorio de ubicaciones");
	}

	@Override
	public List<LocationDomains.Level> execute() {
		return locations.findLevels().stream().map(level -> new LocationDomains.Level(level.depth(), level.name())).toList();
	}

}
