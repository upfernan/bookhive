package co.com.bookhive.circulation.services.usecase.location.configurelocationlevels;

import java.util.List;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface ConfigureLocationLevelsUseCase {

	List<LocationDomains.Level> execute(LocationDomains.Levels levels);

}
