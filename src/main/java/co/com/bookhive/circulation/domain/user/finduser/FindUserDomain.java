package co.com.bookhive.circulation.domain.user.finduser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.com.bookhive.circulation.domain.account.finduseraccount.UserAccountDomain;
import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.loan.returnbook.DailyRateFinePolicy;
import co.com.bookhive.circulation.domain.loan.returnbook.FinePolicy;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

public final class FindUserDomain {

	public static final int MAX_EMAIL_LENGTH = 254;

	private String email;

	private UserRepository userRepository;
	private FineRepository fineRepository;
	private LoanRepository loanRepository;
	private InstitutionParametersRepository institutionParametersRepository;
	private ReservationRepository reservationRepository;
	private BookCopyRepository bookCopyRepository;

	public FindUserDomain(final String email) {
		super();
		setEmail(email);
	}

	public FindUserDomain withRepositories(
			final UserRepository userRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository,
			final InstitutionParametersRepository institutionParametersRepository) {

		final FindUserDomain withPorts = new FindUserDomain(email);
		withPorts.userRepository = userRepository;
		withPorts.fineRepository = fineRepository;
		withPorts.loanRepository = loanRepository;
		withPorts.institutionParametersRepository = institutionParametersRepository;
		withPorts.reservationRepository = reservationRepository;
		withPorts.bookCopyRepository = bookCopyRepository;

		return withPorts;
	}

	public FindUserDomain withReservations(
			final ReservationRepository reservations,
			final BookCopyRepository bookCopies) {

		final FindUserDomain withPorts = withRepositories(
				userRepository,
				fineRepository,
				loanRepository,
				institutionParametersRepository);
		withPorts.reservationRepository = reservations;
		withPorts.bookCopyRepository = bookCopies;

		return withPorts;
	}

	public void validate() {

		requireRepositories();

		validateEmail();
	}

	public FoundUserDomain find() {

		requireRepositories();

		final InstitutionParametersEntity parameters = findParameters();
		final int maxLoans = parameters.getMaxConcurrentLoans();
		final UserEntity user = findUser();

		final List<LoanEntity> loans = loanRepository.findByCriteria(LoanCriteria.activeByUser(user.getId()));
		final int activeLoans = loans.size();
		final int pendingFines = fineRepository.findByCriteria(
				FineCriteria.pendingByUser(user.getId())).size();

		return new FoundUserDomain(
				user.getId(),
				user.getName(),
				user.getEmail(),
				activeLoans,
				maxLoans,
				pendingFines,
				blocksOf(user.getStatus(), activeLoans, maxLoans, pendingFines),
				readyReservationsOf(user.getId()),
				openLoansOf(loans, parameters));
	}

	public String getEmail() {
		return email;
	}

	private void validateEmail() {

		if (TextHelper.isDefaultValue(email)) {
			throw BookHiveException.create(
					MessageKey.USER_EMAIL_REQUIRED,
					"El correo del usuario llegó vacío en FindUserDomain.");
		}
	}

	private InstitutionParametersEntity findParameters() {

		final InstitutionParametersEntity parameters = institutionParametersRepository
				.findAll(PageCriteria.of(0, 1)).stream()
				.findFirst()
				.orElseGet(InstitutionParametersEntity::new);

		final int days = parameters.getLoanDays();
		final int max = parameters.getMaxConcurrentLoans();

		if (days <= 0 || max <= 0) {
			throw BookHiveException.createConflict(
					MessageKey.INSTITUTION_WITHOUT_PARAMETERS,
					"institution_parameters entregó plazo de " + days + " días y máximo de "
							+ max + " préstamos; sin esas reglas no se puede decir si el usuario"
							+ " puede llevar ejemplares.");
		}

		return parameters;
	}

	private UserEntity findUser() {

		if (email.length() > MAX_EMAIL_LENGTH) {
			throw BookHiveException.createConflict(
					MessageKey.USER_NOT_FOUND,
					"El correo buscado tiene " + email.length() + " caracteres y la columna admite "
							+ MAX_EMAIL_LENGTH + "; ningún usuario puede tenerlo.");
		}

		final List<UserEntity> matches = userRepository.findByCriteria(UserCriteria.byEmail(email));

		final Optional<UserEntity> writtenTheSame = matches.stream()
				.filter(user -> email.equals(user.getEmail()))
				.findFirst();

		if (writtenTheSame.isPresent()) {
			return writtenTheSame.get();
		}

		if (matches.size() == 1) {
			return matches.get(0);
		}

		throw userNotFound(matches.size());
	}

	private List<ReadyReservationDomain> readyReservationsOf(final UUID user) {

		return reservationRepository.findByCriteria(ReservationCriteria.activeOfUser(user)).stream()
				.filter(ReservationEntity::isAssigned)
				.map(reservation -> new ReadyReservationDomain(
						reservation.getId(),
						reservation.getBook(),
						reservation.getBookCopy(),
						barcodeOf(reservation.getBookCopy()),
						reservation.getPickupDeadline()))
				.sorted(Comparator.comparing(ReadyReservationDomain::pickupDeadline))
				.toList();
	}

	private String barcodeOf(final UUID bookCopy) {

		return bookCopyRepository.findById(bookCopy)
				.map(BookCopyEntity::getBarcode)
				.orElse(TextHelper.getDefaultValue());
	}

	private static List<OpenLoanDomain> openLoansOf(
			final List<LoanEntity> loans,
			final InstitutionParametersEntity parameters) {

		final FinePolicy policy = new DailyRateFinePolicy(parameters.getDailyFineRate());
		final LocalDate today = CurrentInstitution.today(parameters);

		return loans.stream()
				.map(loan -> {
					final UserAccountDomain.ActiveLoan active = UserAccountDomain.ActiveLoan.on(
							loan.getId(), loan.getBookCopy().getBook(), loan.getLoanDate(), loan.getDueDate(), today, policy);
					return new OpenLoanDomain(active.loan(), loan.getBookCopy().getId(), loan.getBookCopy().getBarcode(),
							active.book(), active.loanDate(), active.dueDate(), active.overdueDays(), active.accruedFine());
				})
				.sorted(Comparator.comparing(OpenLoanDomain::dueDate))
				.toList();
	}

	private static BookHiveException userNotFound(final int matches) {

		if (matches == 0) {
			return BookHiveException.createConflict(
					MessageKey.USER_NOT_FOUND,
					"No hay un usuario con el correo buscado en la institución en curso.");
		}

		return BookHiveException.createConflict(
				MessageKey.USER_NOT_FOUND,
				"El correo buscado coincide con " + matches + " usuarios si no se distinguen mayúsculas"
						+ " y ninguno lo tiene escrito igual; no se elige uno al azar.");
	}

	private static List<String> blocksOf(
			final String status,
			final int activeLoans,
			final int maxLoans,
			final int pendingFines) {

		final List<String> blocks = new ArrayList<>();

		if (UserEntity.STATUS_RETIRED.equals(status)) {
			blocks.add(MessageKey.USER_RETIRED);
		} else if (!UserEntity.STATUS_ACTIVE.equals(status)) {
			blocks.add(MessageKey.USER_SUSPENDED);
		}

		if (pendingFines > 0) {
			blocks.add(MessageKey.USER_WITH_PENDING_FINES);
		}

		if (activeLoans >= maxLoans) {
			blocks.add(MessageKey.USER_AT_LOAN_LIMIT);
		}

		return List.copyOf(blocks);
	}

	private void requireRepositories() {

		if (ObjectHelper.isNull(userRepository)
				|| ObjectHelper.isNull(fineRepository)
				|| ObjectHelper.isNull(loanRepository)
				|| ObjectHelper.isNull(institutionParametersRepository)
				|| ObjectHelper.isNull(reservationRepository)
				|| ObjectHelper.isNull(bookCopyRepository)) {

			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se usó FindUserDomain sin repositorios; falta llamar withRepositories.");
		}
	}

	private void setEmail(final String email) {
		this.email = TextHelper.getDefaultValue(email);
	}

}
