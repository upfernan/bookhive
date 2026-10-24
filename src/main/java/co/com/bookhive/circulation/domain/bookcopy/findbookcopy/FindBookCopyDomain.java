package co.com.bookhive.circulation.domain.bookcopy.findbookcopy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.com.bookhive.circulation.domain.bookcopy.BookCopyLendingRule;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindBookCopyDomain {

	public static final int MAX_BARCODE_LENGTH = 50;

	private String barcode;
	private UUID user;

	private BookCopyRepository bookCopyRepository;
	private ReservationRepository reservationRepository;

	public FindBookCopyDomain(final String barcode) {
		this(barcode, null);
	}

	public FindBookCopyDomain(final String barcode, final UUID user) {
		super();
		setBarcode(barcode);
		setUser(user);
	}

	public FindBookCopyDomain withRepositories(final BookCopyRepository bookCopyRepository) {
		return withRepositories(bookCopyRepository, reservationRepository);
	}

	public FindBookCopyDomain withRepositories(
			final BookCopyRepository bookCopyRepository,
			final ReservationRepository reservationRepository) {

		final FindBookCopyDomain withPorts = new FindBookCopyDomain(barcode, user);
		withPorts.bookCopyRepository = bookCopyRepository;
		withPorts.reservationRepository = reservationRepository;

		return withPorts;
	}

	public void validate() {

		requireRepositories();

		validateBarcode();
	}

	public FoundBookCopyDomain find() {

		requireRepositories();

		final BookCopyEntity bookCopy = findBookCopy();
		final String availabilityBlock = BookCopyLendingRule.blockFor(bookCopy, user, reservationRepository)
				.orElse(TextHelper.getDefaultValue());

		return new FoundBookCopyDomain(
				bookCopy.getId(),
				bookCopy.getBook(),
				bookCopy.getBarcode(),
				TextHelper.isDefaultValue(availabilityBlock),
				bookCopy.isLendable(),
				blocksOf(availabilityBlock, bookCopy.isLendable()));
	}

	public String getBarcode() {
		return barcode;
	}

	private void validateBarcode() {

		if (TextHelper.isDefaultValue(barcode)) {
			throw BookHiveException.create(
					MessageKey.BOOK_COPY_BARCODE_REQUIRED,
					"El código del ejemplar llegó vacío en FindBookCopyDomain.");
		}
	}

	private BookCopyEntity findBookCopy() {

		if (barcode.length() > MAX_BARCODE_LENGTH) {
			throw BookHiveException.createConflict(
					MessageKey.BOOK_COPY_NOT_FOUND,
					"El código buscado tiene " + barcode.length() + " caracteres y la columna admite "
							+ MAX_BARCODE_LENGTH + "; ningún ejemplar puede tenerlo.");
		}

		return bookCopyRepository.findByCriteria(BookCopyCriteria.byBarcode(barcode)).stream()
				.findFirst()
				.orElseThrow(() -> BookHiveException.createConflict(
						MessageKey.BOOK_COPY_NOT_FOUND,
						"No hay un ejemplar con el código " + barcode + " en la institución en curso."));
	}

	private static List<String> blocksOf(final String availabilityBlock, final boolean lendable) {

		final List<String> blocks = new ArrayList<>();

		if (!TextHelper.isDefaultValue(availabilityBlock)) {
			blocks.add(availabilityBlock);
		}

		if (!lendable) {
			blocks.add(MessageKey.BOOK_COPY_REFERENCE_ONLY);
		}

		return List.copyOf(blocks);
	}

	private void requireRepositories() {

		if (ObjectHelper.isNull(bookCopyRepository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se usó FindBookCopyDomain sin repositorio; falta llamar withRepositories.");
		}
	}

	private void setBarcode(final String barcode) {
		this.barcode = Optional.ofNullable(barcode).map(String::trim).orElse(TextHelper.getDefaultValue());
	}

	private void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
	}

}
