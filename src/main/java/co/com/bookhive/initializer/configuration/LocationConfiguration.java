package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulation.infrastructure.input.controllers.LocationController;
import co.com.bookhive.circulation.services.input.adapters.LocationInputAdapters;
import co.com.bookhive.circulation.services.input.ports.ListBranchesInputPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.usecase.location.configurelocationlevels.impl.ConfigureLocationLevelsUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.createbranch.impl.CreateBranchUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.findlocationlevels.impl.FindLocationLevelsUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.listbranches.impl.ListBranchesUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.relocatebookcopy.impl.RelocateBookCopyUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.withdrawbookcopy.impl.WithdrawBookCopyUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class LocationConfiguration {

	@Bean
	ListBranchesInputPort listBranchesInputPort(final LocationRepository locations) {
		return LocationInputAdapters.listBranches(new ListBranchesUseCaseImpl(locations));
	}

	@Bean
	LocationController.Ports locationPorts(
			final LocationRepository locations,
			final BookCopyRepository bookCopies,
			final SessionContextPort session,
			final AuditPort audit,
			final ListBranchesInputPort listBranches) {

		return new LocationController.Ports(
				LocationInputAdapters.findLevels(new FindLocationLevelsUseCaseImpl(locations)),
				LocationInputAdapters.configureLevels(new ConfigureLocationLevelsUseCaseImpl(locations, session, audit)),
				listBranches,
				LocationInputAdapters.createBranch(new CreateBranchUseCaseImpl(locations, session, audit)),
				LocationInputAdapters.findLocation(new FindBookCopyLocationUseCaseImpl(bookCopies, locations)),
				LocationInputAdapters.relocate(new RelocateBookCopyUseCaseImpl(bookCopies, locations, session, audit)),
				LocationInputAdapters.withdraw(new WithdrawBookCopyUseCaseImpl(bookCopies, locations, session, audit)));
	}

}
