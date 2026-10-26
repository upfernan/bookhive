package co.com.bookhive.circulation.services.usecase.location.findlocationlevels;

import java.util.List;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface FindLocationLevelsUseCase {

	List<LocationDomains.Level> execute();

}
