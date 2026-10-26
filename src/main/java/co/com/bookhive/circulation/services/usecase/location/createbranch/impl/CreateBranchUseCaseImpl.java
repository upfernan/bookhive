package co.com.bookhive.circulation.services.usecase.location.createbranch.impl;

import java.util.Locale;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities;
import co.com.bookhive.circulation.services.usecase.location.createbranch.CreateBranchUseCase;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class CreateBranchUseCaseImpl implements CreateBranchUseCase {

	private final LocationRepository locations;
	private final SessionContextPort session;
	private final AuditPort audit;

	public CreateBranchUseCaseImpl(final LocationRepository locations, final SessionContextPort session, final AuditPort audit) {
		super();
		this.locations = FindBookCopyLocationUseCaseImpl.requireDependency(locations, "el repositorio de ubicaciones");
		this.session = FindBookCopyLocationUseCaseImpl.requireDependency(session, "el contexto de la sesión");
		this.audit = FindBookCopyLocationUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public LocationDomains.Branch execute(final LocationDomains.Branch input) {
		final LocationDomains.Branch requested = ObjectHelper.getDefaultValue(input, new LocationDomains.Branch(null, null));
		requested.validate();
		final String key = requested.name().toLowerCase(Locale.ROOT);
		if (locations.findBranches().stream().anyMatch(branch -> branch.name().toLowerCase(Locale.ROOT).equals(key))) {
			throw BookHiveException.createConflict(MessageKey.BRANCH_NAME_TAKEN, "Ya existe la sede " + requested.name() + ".");
		}
		final LocationDomains.Branch branch = new LocationDomains.Branch(UUIDHelper.generateNew(), requested.name());
		locations.createBranch(new LocationEntities.BranchEntity(branch.id(), branch.name()));
		audit.record(AuditPort.BRANCH_CREATED, session.getInstitution(), session.getLibrarian(), branch.name());
		return branch;
	}

}
