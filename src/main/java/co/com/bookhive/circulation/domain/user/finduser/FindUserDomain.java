package co.com.bookhive.circulation.domain.user.finduser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
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

		return withPorts;
	}

	public void validate() {

		requireRepositories();

		validateEmail();
	}

	public FoundUserDomain find() {

		requireRepositories();

		final int maxLoans = findMaxConcurrentLoans();
		final UserEntity user = findUser();

		final int activeLoans = loanRepository.findByCriteria(
				LoanCriteria.activeByUser(user.getId())).size();
		final int pendingFines = fineRepository.findByCriteria(
				FineCriteria.pendingByUser(user.getId())).size();

		return new FoundUserDomain(
				user.getId(),
				user.getName(),
				user.getEmail(),
				activeLoans,
				maxLoans,
				pendingFines,
				blocksOf(activeLoans, maxLoans, pendingFines));
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

	private int findMaxConcurrentLoans() {

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

		return max;
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

	private static List<String> blocksOf(final int activeLoans, final int maxLoans, final int pendingFines) {

		final List<String> blocks = new ArrayList<>();

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
				|| ObjectHelper.isNull(institutionParametersRepository)) {

			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se usó FindUserDomain sin repositorios; falta llamar withRepositories.");
		}
	}

	private void setEmail(final String email) {
		this.email = TextHelper.getDefaultValue(email);
	}

}
