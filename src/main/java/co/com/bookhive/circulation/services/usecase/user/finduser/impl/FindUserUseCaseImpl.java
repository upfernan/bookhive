package co.com.bookhive.circulation.services.usecase.user.finduser.impl;

import co.com.bookhive.circulation.domain.user.finduser.FindUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.usecase.user.finduser.FindUserUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindUserUseCaseImpl implements FindUserUseCase {

	private final UserRepository userRepository;
	private final FineRepository fineRepository;
	private final LoanRepository loanRepository;
	private final InstitutionParametersRepository institutionParametersRepository;
	private final ReservationRepository reservationRepository;
	private final BookCopyRepository bookCopyRepository;

	public FindUserUseCaseImpl(
			final UserRepository userRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository,
			final InstitutionParametersRepository institutionParametersRepository,
			final ReservationRepository reservationRepository,
			final BookCopyRepository bookCopyRepository) {

		super();
		this.userRepository = requireDependency(userRepository, "el repositorio de usuarios");
		this.fineRepository = requireDependency(fineRepository, "el repositorio de multas");
		this.loanRepository = requireDependency(loanRepository, "el repositorio de préstamos");
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.reservationRepository = requireDependency(reservationRepository, "el repositorio de reservas");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
	}

	@Override
	public FoundUserDomain execute(final FindUserDomain domain) {

		final FindUserDomain search = ObjectHelper.getDefaultValue(domain, new FindUserDomain(""))
				.withRepositories(
						userRepository,
						fineRepository,
						loanRepository,
						institutionParametersRepository)
				.withReservations(reservationRepository, bookCopyRepository);

		search.validate();

		return search.find();
	}

	private static <T> T requireDependency(final T dependency, final String name) {

		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso FindUser se creó sin " + name + ".");
		}

		return dependency;
	}

}
