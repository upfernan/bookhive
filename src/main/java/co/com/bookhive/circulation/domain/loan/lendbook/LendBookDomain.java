package co.com.bookhive.circulation.domain.loan.lendbook;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.com.bookhive.circulation.domain.bookcopy.BookCopyLendingRule;
import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.user.BorrowerChecks;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class LendBookDomain {

	public static final String NOTICE_KIND = "PRESTAMO_REGISTRADO";

	private UUID id;
	private UUID bookCopy;
	private UUID user;
	private UUID institution;
	private UUID librarian;
	private LocalDate loanDate;
	private LocalDate dueDate;

	private UserRepository userRepository;
	private BookCopyRepository bookCopyRepository;
	private FineRepository fineRepository;
	private LoanRepository loanRepository;
	private InstitutionParametersRepository institutionParametersRepository;
	private ReservationRepository reservationRepository;

	public LendBookDomain(
			final UUID id,
			final UUID bookCopy,
			final UUID user,
			final UUID institution,
			final UUID librarian,
			final LocalDate loanDate,
			final LocalDate dueDate) {

		super();
		setId(id);
		setBookCopy(bookCopy);
		setUser(user);
		setInstitution(institution);
		setLibrarian(librarian);
		setLoanDate(loanDate);
		setDueDate(dueDate);
	}

	public LendBookDomain withDueDate(final LocalDate newDueDate) {
		return copy(newDueDate);
	}

	public LendBookDomain withRepositories(
			final UserRepository userRepository,
			final BookCopyRepository bookCopyRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository,
			final InstitutionParametersRepository institutionParametersRepository) {

		final LendBookDomain withPorts = copy(dueDate);
		withPorts.userRepository = userRepository;
		withPorts.bookCopyRepository = bookCopyRepository;
		withPorts.fineRepository = fineRepository;
		withPorts.loanRepository = loanRepository;
		withPorts.institutionParametersRepository = institutionParametersRepository;
		withPorts.reservationRepository = reservationRepository;

		return withPorts;
	}

	public LendBookDomain withReservations(final ReservationRepository reservations) {

		final LendBookDomain withPorts = withRepositories(
				userRepository,
				bookCopyRepository,
				fineRepository,
				loanRepository,
				institutionParametersRepository);
		withPorts.reservationRepository = reservations;

		return withPorts;
	}

	public void validate() {

		requireRepositories();

		validateBookCopy();
		validateUser();
		validateLibrarian();
		validateInstitution();
		validateLoanDate();

		validateInstitutionParameters();
		validateBookCopyExists();
		validateBookCopyIsAvailable();
		validateBookCopyIsLendable();
		validateBorrower();
		validateUserUnderLoanLimit();
	}

	public LocalDate calculateDueDate() {

		requireRepositories();
		validateLoanDate();
		validateInstitutionParameters();

		return loanDate.plusDays(findInstitutionParameters().getLoanDays());
	}

	public UUID getId() {
		return id;
	}

	public UUID getBookCopy() {
		return bookCopy;
	}

	public UUID getUser() {
		return user;
	}

	public UUID getInstitution() {
		return institution;
	}

	public UUID getLibrarian() {
		return librarian;
	}

	public LocalDate getLoanDate() {
		return loanDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	private void validateBookCopy() {

		if (UUIDHelper.isDefaultValue(bookCopy)) {
			throw BookHiveException.create(
					MessageKey.LOAN_BOOK_COPY_REQUIRED,
					"El identificador del ejemplar llegó vacío en LendBookDomain.");
		}
	}

	private void validateUser() {

		if (UUIDHelper.isDefaultValue(user)) {
			throw BookHiveException.create(
					MessageKey.LOAN_USER_REQUIRED,
					"El identificador del usuario llegó vacío en LendBookDomain.");
		}
	}

	private void validateLibrarian() {

		if (UUIDHelper.isDefaultValue(librarian)) {
			throw BookHiveException.create(
					MessageKey.LOAN_LIBRARIAN_REQUIRED,
					"El bibliotecario llegó vacío en LendBookDomain; debe resolverse desde el token.");
		}
	}

	private void validateInstitution() {

		if (UUIDHelper.isDefaultValue(institution)) {
			throw BookHiveException.create(
					MessageKey.LOAN_INSTITUTION_REQUIRED,
					"La institución llegó vacía en LendBookDomain; debe resolverse desde el token.");
		}
	}

	private void validateLoanDate() {

		if (DateHelper.isDefaultValue(loanDate)) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATE_REQUIRED,
					"La fecha del préstamo llegó vacía en LendBookDomain; la fija el servidor.");
		}

		if (loanDate.isAfter(CurrentInstitution.today(findInstitutionParameters()))) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATE_IN_THE_FUTURE,
					"La fecha del préstamo es posterior a la fecha actual.");
		}
	}

	private void validateInstitutionParameters() {

		final InstitutionParametersEntity parameters = findInstitutionParameters();
		final int days = parameters.getLoanDays();
		final int max = parameters.getMaxConcurrentLoans();

		if (days <= 0 || max <= 0) {
			throw BookHiveException.createConflict(
					MessageKey.INSTITUTION_WITHOUT_PARAMETERS,
					"institution_parameters entregó plazo de " + days + " días y máximo de "
							+ max + " préstamos; el cero indica que la fila no existe"
							+ " o quedó sin configurar para el esquema en curso.");
		}
	}

	private void validateBookCopyExists() {

		if (UUIDHelper.isDefaultValue(findBookCopy().getId())) {
			throw BookHiveException.createConflict(
					MessageKey.BOOK_COPY_NOT_FOUND,
					"No se encontró el ejemplar " + bookCopy + " en la institución en curso.");
		}
	}

	private void validateBookCopyIsAvailable() {

		final BookCopyEntity found = findBookCopy();
		final Optional<String> block = BookCopyLendingRule.blockFor(found, user, reservationRepository);

		if (block.isPresent()) {
			throw BookHiveException.createConflict(
					block.get(),
					"El ejemplar " + bookCopy + " está en estado " + found.getStatus()
							+ " y para prestarse debe estar " + BookCopyEntity.STATUS_AVAILABLE
							+ ", o " + BookCopyEntity.STATUS_RESERVED + " para el usuario " + user + ".");
		}
	}

	private void validateBookCopyIsLendable() {

		if (!findBookCopy().isLendable()) {
			throw BookHiveException.createConflict(
					MessageKey.BOOK_COPY_REFERENCE_ONLY,
					"El ejemplar " + bookCopy + " está marcado como de consulta en sala.");
		}
	}

	private void validateBorrower() {
		final UserEntity found = BorrowerChecks.requireExisting(
				userRepository.findById(user), user, MessageKey.LOAN_USER_NOT_FOUND);
		BorrowerChecks.requireActive(found);
		BorrowerChecks.requireWithoutPendingFines(fineRepository, user);
	}

	private void validateUserUnderLoanLimit() {

		final int max = findInstitutionParameters().getMaxConcurrentLoans();

		final List<LoanEntity> activeLoans = loanRepository.findByCriteria(
				LoanCriteria.activeByUser(user));

		if (activeLoans.size() >= max) {
			throw BookHiveException.createConflict(
					MessageKey.USER_AT_LOAN_LIMIT,
					"El usuario " + user + " tiene " + activeLoans.size()
							+ " préstamos activos y el máximo de la institución es " + max + ".");
		}
	}

	private BookCopyEntity findBookCopy() {
		return bookCopyRepository.findById(bookCopy).orElseGet(BookCopyEntity::new);
	}

	private InstitutionParametersEntity findInstitutionParameters() {
		return CurrentInstitution.parameters(institutionParametersRepository);
	}

	private void requireRepositories() {

		if (ObjectHelper.isNull(userRepository)
				|| ObjectHelper.isNull(bookCopyRepository)
				|| ObjectHelper.isNull(fineRepository)
				|| ObjectHelper.isNull(loanRepository)
				|| ObjectHelper.isNull(institutionParametersRepository)) {

			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"Se usó LendBookDomain sin repositorios; falta llamar withRepositories.");
		}
	}

	private LendBookDomain copy(final LocalDate newDueDate) {
		return new LendBookDomain(
				id, bookCopy, user, institution,
				librarian, loanDate, newDueDate);
	}

	private void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	private void setBookCopy(final UUID bookCopy) {
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
	}

	private void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
	}

	private void setInstitution(final UUID institution) {
		this.institution = UUIDHelper.getDefaultValue(institution);
	}

	private void setLibrarian(final UUID librarian) {
		this.librarian = UUIDHelper.getDefaultValue(librarian);
	}

	private void setLoanDate(final LocalDate loanDate) {
		this.loanDate = DateHelper.getDefaultValue(loanDate);
	}

	private void setDueDate(final LocalDate dueDate) {
		this.dueDate = DateHelper.getDefaultValue(dueDate);
	}

}
