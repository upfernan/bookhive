package co.com.bookhive.catalog.services.usecase.account.viewmyaccount.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import co.com.bookhive.catalog.domain.account.viewmyaccount.MyAccountDomain;
import co.com.bookhive.catalog.services.output.ports.UserAccountPort;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.usecase.account.viewmyaccount.ViewMyAccountUseCase;
import co.com.bookhive.catalog.services.usecase.reservation.listmyreservations.ListMyReservationsUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ViewMyAccountUseCaseImpl implements ViewMyAccountUseCase {

	private static final String AUTHOR_SEPARATOR = ", ";

	private final UserAccountPort userAccount;
	private final ListMyReservationsUseCase listMyReservations;
	private final BookRepository bookRepository;

	public ViewMyAccountUseCaseImpl(
			final UserAccountPort userAccount,
			final ListMyReservationsUseCase listMyReservations,
			final BookRepository bookRepository) {

		super();
		this.userAccount = requireDependency(userAccount, "la cuenta del usuario");
		this.listMyReservations = requireDependency(listMyReservations, "las reservas del usuario");
		this.bookRepository = requireDependency(bookRepository, "el repositorio de libros");
	}

	@Override
	public MyAccountDomain execute() {

		final MyAccountDomain account = userAccount.findMine();
		final List<UUID> books = account.books();
		final Map<UUID, MyAccountDomain.Book> named = books.isEmpty()
				? Map.of()
				: bookRepository.findByCriteria(BookCriteria.byIds(books)).stream()
						.map(book -> new MyAccountDomain.Book(book.id(), book.title(), String.join(AUTHOR_SEPARATOR, book.authors())))
						.collect(Collectors.toMap(MyAccountDomain.Book::id, Function.identity(), (first, second) -> first));

		return account.with(named, listMyReservations.execute());
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED, "El caso de uso ViewMyAccount se creó sin " + name + ".");
		}
		return dependency;
	}

}
