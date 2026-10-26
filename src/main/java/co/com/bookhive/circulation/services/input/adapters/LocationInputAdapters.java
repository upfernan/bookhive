package co.com.bookhive.circulation.services.input.adapters;

import java.util.List;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.input.dto.request.BranchRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.LocationLevelsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.RelocateBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;
import co.com.bookhive.circulation.services.input.ports.ConfigureLocationLevelsInputPort;
import co.com.bookhive.circulation.services.input.ports.CreateBranchInputPort;
import co.com.bookhive.circulation.services.input.ports.FindBookCopyLocationInputPort;
import co.com.bookhive.circulation.services.input.ports.FindLocationLevelsInputPort;
import co.com.bookhive.circulation.services.input.ports.ListBranchesInputPort;
import co.com.bookhive.circulation.services.input.ports.RelocateBookCopyInputPort;
import co.com.bookhive.circulation.services.input.ports.WithdrawBookCopyInputPort;
import co.com.bookhive.circulation.services.usecase.location.configurelocationlevels.ConfigureLocationLevelsUseCase;
import co.com.bookhive.circulation.services.usecase.location.createbranch.CreateBranchUseCase;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.FindBookCopyLocationUseCase;
import co.com.bookhive.circulation.services.usecase.location.findlocationlevels.FindLocationLevelsUseCase;
import co.com.bookhive.circulation.services.usecase.location.listbranches.ListBranchesUseCase;
import co.com.bookhive.circulation.services.usecase.location.relocatebookcopy.RelocateBookCopyUseCase;
import co.com.bookhive.circulation.services.usecase.location.withdrawbookcopy.WithdrawBookCopyUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class LocationInputAdapters {

	private LocationInputAdapters() {
		super();
	}

	public static FindLocationLevelsInputPort findLevels(final FindLocationLevelsUseCase useCase) {
		return () -> levels(useCase.execute());
	}

	public static ConfigureLocationLevelsInputPort configureLevels(final ConfigureLocationLevelsUseCase useCase) {
		return request -> levels(useCase.execute(
				new LocationDomains.Levels(ObjectHelper.isNull(request) ? List.of() : request.names())));
	}

	public static ListBranchesInputPort listBranches(final ListBranchesUseCase useCase) {
		return () -> useCase.execute().stream().map(LocationInputAdapters::branch).toList();
	}

	public static CreateBranchInputPort createBranch(final CreateBranchUseCase useCase) {
		return request -> branch(useCase.execute(new LocationDomains.Branch(null,
				ObjectHelper.getDefaultValue(request, new BranchRequestDTO(null)).name())));
	}

	public static FindBookCopyLocationInputPort findLocation(final FindBookCopyLocationUseCase useCase) {
		return barcode -> location(useCase.execute(barcode));
	}

	public static RelocateBookCopyInputPort relocate(final RelocateBookCopyUseCase useCase) {
		return request -> {
			final RelocateBookCopyRequestDTO safe = ObjectHelper.getDefaultValue(request, new RelocateBookCopyRequestDTO(null, null, null));
			return location(useCase.execute(new LocationDomains.Relocation(safe.barcode(), safe.branch(), safe.values())));
		};
	}

	public static WithdrawBookCopyInputPort withdraw(final WithdrawBookCopyUseCase useCase) {
		return barcode -> location(useCase.execute(barcode));
	}

	private static List<LocationResponseDTOs.Level> levels(final List<LocationDomains.Level> levels) {
		return levels.stream().map(level -> new LocationResponseDTOs.Level(level.depth(), level.name())).toList();
	}

	private static LocationResponseDTOs.Branch branch(final LocationDomains.Branch branch) {
		return new LocationResponseDTOs.Branch(branch.id(), branch.name());
	}

	private static LocationResponseDTOs.BookCopyLocation location(final LocationDomains.BookCopyLocation location) {
		return new LocationResponseDTOs.BookCopyLocation(location.bookCopy(), location.barcode(), location.status(),
				branch(location.branch()),
				location.spots().stream().map(spot -> new LocationResponseDTOs.Spot(spot.depth(), spot.level(), spot.value())).toList(),
				location.describe());
	}

}
