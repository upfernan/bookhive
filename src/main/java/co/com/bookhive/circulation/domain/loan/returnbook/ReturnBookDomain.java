package co.com.bookhive.circulation.domain.loan.returnbook;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

public final class ReturnBookDomain {

	public static final String NOTICE_KIND = "MULTA_GENERADA";

	private UUID bookCopy;
	private UUID institution;
	private UUID librarian;
	private LocalDate returnDate;

	private BookCopyRepository bookCopyRepository;
	private LoanRepository loanRepository;
	private InstitutionParametersRepository institutionParametersRepository;

	public ReturnBookDomain(
			final UUID bookCopy,
			final UUID institution,
			final UUID librarian,
			final LocalDate returnDate) {

		super();
		setBookCopy(bookCopy);
		setInstitution(institution);
		setLibrarian(librarian);
		setReturnDate(returnDate);
	}

	public ReturnBookDomain withSession(final UUID sessionInstitution, final UUID sessionLibrarian) {
		final ReturnBookDomain withSession = new ReturnBookDomain(bookCopy, sessionInstitution, sessionLibrarian, returnDate);
		withSession.bookCopyRepository = bookCopyRepository;
		withSession.loanRepository = loanRepository;
		withSession.institutionParametersRepository = institutionParametersRepository;
		return withSession;
	}

	public ReturnBookDomain withRepositories(
			final BookCopyRepository bookCopies,
			final LoanRepository loans,
			final InstitutionParametersRepository institutionParameters) {

		final ReturnBookDomain withPorts = new ReturnBookDomain(bookCopy, institution, librarian, returnDate);
		withPorts.bookCopyRepository = bookCopies;
		withPorts.loanRepository = loans;
		withPorts.institutionParametersRepository = institutionParameters;
		return withPorts;
	}

	public void validate() {
		requireRepositories();
		validateBookCopy();
		validateLibrarian();
		validateInstitution();
		validateReturnDate();
		validateInstitutionParameters();
		validateBookCopyExists();
		openLoan();
	}

	public LoanEntity openLoan() {
		requireRepositories();
		final List<LoanEntity> open = loanRepository.findByCriteria(LoanCriteria.openForBookCopy(bookCopy));
		if (open.isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.LOAN_RETURN_NOT_OPEN,
					"El ejemplar " + bookCopy + " no tiene un préstamo activo o vencido en la institución en curso.");
		}
		return open.get(0);
	}

	public int overdueDaysFor(final LocalDate dueDate) {
		if (DateHelper.isDefaultValue(dueDate)) {
			return 0;
		}
		return (int) Math.max(0L, ChronoUnit.DAYS.between(dueDate, returnDate));
	}

	public FinePolicy finePolicy() {
		requireRepositories();
		return new DailyRateFinePolicy(findInstitutionParameters().getDailyFineRate());
	}

	public UUID getBookCopy() {
		return bookCopy;
	}

	public UUID getInstitution() {
		return institution;
	}

	public UUID getLibrarian() {
		return librarian;
	}

	public LocalDate getReturnDate() {
		return returnDate;
	}

	private void validateBookCopy() {
		if (UUIDHelper.isDefaultValue(bookCopy)) {
			throw BookHiveException.create(
					MessageKey.LOAN_RETURN_BOOK_COPY_REQUIRED,
					"El identificador del ejemplar llegó vacío en ReturnBookDomain.");
		}
	}

	private void validateLibrarian() {
		if (UUIDHelper.isDefaultValue(librarian)) {
			throw BookHiveException.create(
					MessageKey.LOAN_LIBRARIAN_REQUIRED,
					"El bibliotecario llegó vacío en ReturnBookDomain; debe resolverse desde la sesión.");
		}
	}

	private void validateInstitution() {
		if (UUIDHelper.isDefaultValue(institution)) {
			throw BookHiveException.create(
					MessageKey.LOAN_INSTITUTION_REQUIRED,
					"La institución llegó vacía en ReturnBookDomain; debe resolverse desde la sesión.");
		}
	}

	private void validateReturnDate() {
		if (DateHelper.isDefaultValue(returnDate)) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATE_REQUIRED,
					"La fecha de devolución llegó vacía en ReturnBookDomain; la fija el servidor.");
		}
		if (returnDate.isAfter(DateHelper.todayInUtc())) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATE_IN_THE_FUTURE,
					"La fecha de devolución es posterior a la fecha actual.");
		}
	}

	private void validateInstitutionParameters() {
		final InstitutionParametersEntity parameters = findInstitutionParameters();
		if (parameters.getLoanDays() <= 0 || parameters.getMaxConcurrentLoans() <= 0) {
			throw BookHiveException.createConflict(
					MessageKey.INSTITUTION_WITHOUT_PARAMETERS,
					"institution_parameters no tiene fila configurada para el esquema en curso;"
							+ " sin ella no se conoce la tarifa por día de retraso.");
		}
	}

	private void validateBookCopyExists() {
		if (bookCopyRepository.findById(bookCopy).isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.BOOK_COPY_NOT_FOUND,
					"No se encontró el ejemplar " + bookCopy + " en la institución en curso.");
		}
	}

	private InstitutionParametersEntity findInstitutionParameters() {
		return institutionParametersRepository.findAll(PageCriteria.of(0, 1)).stream()
				.findFirst()
				.orElseGet(InstitutionParametersEntity::new);
	}

	private void requireRepositories() {
		if (ObjectHelper.isNull(bookCopyRepository)
				|| ObjectHelper.isNull(loanRepository)
				|| ObjectHelper.isNull(institutionParametersRepository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"Se usó ReturnBookDomain sin repositorios; falta llamar withRepositories.");
		}
	}

	private void setBookCopy(final UUID bookCopy) {
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
	}

	private void setInstitution(final UUID institution) {
		this.institution = UUIDHelper.getDefaultValue(institution);
	}

	private void setLibrarian(final UUID librarian) {
		this.librarian = UUIDHelper.getDefaultValue(librarian);
	}

	private void setReturnDate(final LocalDate returnDate) {
		this.returnDate = DateHelper.getDefaultValue(returnDate);
	}

}
