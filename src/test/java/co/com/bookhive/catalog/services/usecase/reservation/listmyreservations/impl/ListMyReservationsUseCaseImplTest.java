package co.com.bookhive.catalog.services.usecase.reservation.listmyreservations.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.domain.reservation.listmyreservations.MyReservationDomain;
import co.com.bookhive.catalog.services.output.ports.entities.UserReservationEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.testdata.FakeCatalog;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class ListMyReservationsUseCaseImplTest {

	private static final Instant REQUESTED_AT = Instant.parse("2026-10-20T10:00:00Z");

	@Test
	@DisplayName("completa cada reserva con el título y los autores del libro")
	void addsTitleAndAuthors() {
		final BookEntity book = new BookEntity(UUID.randomUUID(), "Clean Code", "978-0132350884", 2008, "FISICO",
				BookEntity.STATUS_ACTIVE, "Software", "Prentice Hall", List.of("Robert C. Martin", "Otro Autor"));
		final FakeCatalog.Books books = new FakeCatalog.Books().with(book);
		final UUID reservation = UUID.randomUUID();
		final LocalDate deadline = LocalDate.of(2026, 10, 26);

		final List<MyReservationDomain> mine = new ListMyReservationsUseCaseImpl(
				() -> List.of(new UserReservationEntity(reservation, book.id(), "ASIGNADA", 0, REQUESTED_AT, deadline)),
				books).execute();

		assertEquals(List.of(new MyReservationDomain(reservation, book.id(), "Clean Code",
				"Robert C. Martin, Otro Autor", "ASIGNADA", 0, REQUESTED_AT, deadline)), mine);
		assertEquals(List.of(book.id()), books.searches().get(0).getIds());
	}

	@Test
	@DisplayName("si el libro ya no está en el catálogo la reserva sale sin título")
	void missingBookLeavesEmptyTitle() {
		final UUID book = UUID.randomUUID();

		final List<MyReservationDomain> mine = new ListMyReservationsUseCaseImpl(
				() -> List.of(new UserReservationEntity(UUID.randomUUID(), book, "EN_ESPERA", 2, REQUESTED_AT, null)),
				new FakeCatalog.Books()).execute();

		assertEquals("", mine.get(0).title());
		assertEquals("", mine.get(0).authors());
		assertEquals(2, mine.get(0).position());
	}

	@Test
	@DisplayName("sin reservas no consulta el catálogo")
	void withoutReservationsSkipsTheCatalog() {
		final FakeCatalog.Books books = new FakeCatalog.Books();

		assertTrue(new ListMyReservationsUseCaseImpl(List::of, books).execute().isEmpty());
		assertTrue(books.searches().isEmpty());
	}

	@Test
	@DisplayName("se rechaza crear el caso de uso sin una dependencia")
	void rejectsMissingDependency() {
		final FakeCatalog.Books books = new FakeCatalog.Books();

		assertThrows(BookHiveException.class, () -> new ListMyReservationsUseCaseImpl(null, books));
		assertThrows(BookHiveException.class, () -> new ListMyReservationsUseCaseImpl(List::of, null));
	}

}
