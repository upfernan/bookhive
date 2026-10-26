package co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.FindBookCopyLocationUseCase;

public final class FindBookCopyLocationUseCaseImpl implements FindBookCopyLocationUseCase {

	private final BookCopyRepository bookCopies;
	private final LocationRepository locations;

	public FindBookCopyLocationUseCaseImpl(final BookCopyRepository bookCopies, final LocationRepository locations) {
		super();
		this.bookCopies = requireDependency(bookCopies, "el repositorio de ejemplares");
		this.locations = requireDependency(locations, "el repositorio de ubicaciones");
	}

	@Override
	public LocationDomains.BookCopyLocation execute(final String barcode) {
		return locationOf(findBookCopy(bookCopies, barcode), locations);
	}

	public static BookCopyEntity findBookCopy(final BookCopyRepository bookCopies, final String barcode) {
		final String code = TextHelper.getDefaultValue(barcode).trim();
		if (TextHelper.isDefaultValue(code)) {
			throw BookHiveException.create(MessageKey.BOOK_COPY_BARCODE_REQUIRED, "No llegó el código de barras.");
		}
		return bookCopies.findByCriteria(BookCopyCriteria.byBarcode(code)).stream().findFirst()
				.orElseThrow(() -> BookHiveException.create(MessageKey.BOOK_COPY_NOT_FOUND, "No existe el ejemplar " + code + "."));
	}

	public static LocationDomains.BookCopyLocation locationOf(final BookCopyEntity copy, final LocationRepository locations) {
		final Map<Integer, String> levels = locations.findLevels().stream()
				.collect(Collectors.toMap(LocationEntities.LevelEntity::depth, LocationEntities.LevelEntity::name));
		final String branch = locations.findBranches().stream()
				.filter(found -> found.id().equals(copy.getBranch()))
				.map(LocationEntities.BranchEntity::name)
				.findFirst()
				.orElse(TextHelper.getDefaultValue());
		final List<LocationDomains.Spot> spots = locations.findValues(copy.getId()).stream()
				.map(value -> new LocationDomains.Spot(value.depth(), levels.getOrDefault(value.depth(), ""), value.value()))
				.toList();
		return new LocationDomains.BookCopyLocation(copy.getId(), copy.getBarcode(), copy.getStatus(),
				new LocationDomains.Branch(copy.getBranch(), branch), spots);
	}

	public static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED, "Un caso de uso de ubicación se creó sin " + name + ".");
		}
		return dependency;
	}

}
