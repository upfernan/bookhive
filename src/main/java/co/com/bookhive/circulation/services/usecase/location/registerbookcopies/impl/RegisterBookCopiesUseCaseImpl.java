package co.com.bookhive.circulation.services.usecase.location.registerbookcopies.impl;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.registerbookcopies.RegisterBookCopiesUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class RegisterBookCopiesUseCaseImpl implements RegisterBookCopiesUseCase {

	private final LocationRepository locations;

	public RegisterBookCopiesUseCaseImpl(final LocationRepository locations) {
		super();
		this.locations = FindBookCopyLocationUseCaseImpl.requireDependency(locations, "el repositorio de ubicaciones");
	}

	@Override
	public int execute(final LocationDomains.NewCopies input) {
		final LocationDomains.NewCopies copies = ObjectHelper.getDefaultValue(input, new LocationDomains.NewCopies(null, null, null));
		copies.validate();
		if (locations.findBranches().stream().noneMatch(branch -> branch.id().equals(copies.branch()))) {
			throw BookHiveException.create(MessageKey.BRANCH_NOT_FOUND, "No existe la sede " + copies.branch() + ".");
		}
		if (locations.anyBarcodeTaken(copies.barcodes())) {
			throw BookHiveException.createConflict(MessageKey.BOOK_COPY_BARCODE_TAKEN,
					"Alguno de los códigos " + copies.barcodes() + " ya existe.");
		}
		locations.addBookCopies(copies.book(), copies.branch(), copies.barcodes());
		return copies.barcodes().size();
	}

}
