package co.com.bookhive.circulation.services.usecase.location.configurelocationlevels.impl;

import java.util.List;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities;
import co.com.bookhive.circulation.services.usecase.location.configurelocationlevels.ConfigureLocationLevelsUseCase;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class ConfigureLocationLevelsUseCaseImpl implements ConfigureLocationLevelsUseCase {

	private final LocationRepository locations;
	private final SessionContextPort session;
	private final AuditPort audit;

	public ConfigureLocationLevelsUseCaseImpl(
			final LocationRepository locations, final SessionContextPort session, final AuditPort audit) {
		super();
		this.locations = FindBookCopyLocationUseCaseImpl.requireDependency(locations, "el repositorio de ubicaciones");
		this.session = FindBookCopyLocationUseCaseImpl.requireDependency(session, "el contexto de la sesión");
		this.audit = FindBookCopyLocationUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public List<LocationDomains.Level> execute(final LocationDomains.Levels input) {
		final LocationDomains.Levels levels = ObjectHelper.getDefaultValue(input, new LocationDomains.Levels(List.of()));
		levels.validate();
		if (locations.hasValuesDeeperThan(levels.names().size())) {
			throw BookHiveException.createConflict(MessageKey.LOCATION_LEVELS_IN_USE,
					"Hay ejemplares ubicados más abajo del nivel " + levels.names().size() + ".");
		}
		locations.replaceLevels(levels.levels().stream()
				.map(level -> new LocationEntities.LevelEntity(level.depth(), level.name())).toList());
		audit.register(AuditPort.LOCATION_LEVELS_CONFIGURED, session.getInstitution(), session.getLibrarian(),
				String.join(" / ", levels.names()));
		return levels.levels();
	}

}
