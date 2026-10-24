package co.com.bookhive.catalog.services.usecase.reservation.listmyreservations.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import co.com.bookhive.catalog.domain.reservation.listmyreservations.MyReservationDomain;
import co.com.bookhive.catalog.services.output.ports.UserReservationsPort;
import co.com.bookhive.catalog.services.output.ports.entities.UserReservationEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.usecase.reservation.listmyreservations.ListMyReservationsUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ListMyReservationsUseCaseImpl implements ListMyReservationsUseCase {

	private static final String AUTHOR_SEPARATOR = ", ";

	private final UserReservationsPort userReservations;
	private final BookRepository bookRepository;

	public ListMyReservationsUseCaseImpl(
			final UserReservationsPort userReservations,
			final BookRepository bookRepository) {

		super();
		this.userReservations = requireDependency(userReservations, "las reservas del usuario");
		this.bookRepository = requireDependency(bookRepository, "el repositorio de libros");
	}

	@Override
	public List<MyReservationDomain> execute() {

		final List<UserReservationEntity> mine = userReservations.findMine();
		if (mine.isEmpty()) {
			return List.of();
		}

		final List<UUID> bookIds = mine.stream().map(UserReservationEntity::book).distinct().toList();
		final Map<UUID, BookEntity> books = bookRepository.findByCriteria(BookCriteria.byIds(bookIds)).stream()
				.collect(Collectors.toMap(BookEntity::id, Function.identity(), (first, second) -> first));

		return mine.stream()
				.map(reservation -> {
					final BookEntity book = books.getOrDefault(
							reservation.book(),
							new BookEntity(reservation.book(), "", "", 0, "", "", "", "", List.of()));
					return new MyReservationDomain(
							reservation.reservation(),
							reservation.book(),
							book.title(),
							String.join(AUTHOR_SEPARATOR, book.authors()),
							reservation.status(),
							reservation.position(),
							reservation.requestedAt(),
							reservation.pickupDeadline());
				})
				.toList();
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso ListMyReservations se creó sin " + name + ".");
		}
		return dependency;
	}

}
