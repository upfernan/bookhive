package co.com.bookhive.catalog.services.usecase.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.com.bookhive.catalog.domain.account.viewmyaccount.MyAccountDomain;
import co.com.bookhive.catalog.domain.reservation.listmyreservations.MyReservationDomain;
import co.com.bookhive.catalog.infrastructure.input.controllers.MyAccountController;
import co.com.bookhive.catalog.infrastructure.output.externalservices.circulation.CirculationUserAccountAdapter;
import co.com.bookhive.catalog.services.input.adapters.ViewMyAccountInputAdapter;
import co.com.bookhive.catalog.services.input.dto.response.MyAccountResponseDTO;
import co.com.bookhive.catalog.services.usecase.account.viewmyaccount.impl.ViewMyAccountUseCaseImpl;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.testdata.FakeCatalog;
import co.com.bookhive.circulation.services.input.dto.response.UserAccountResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class ViewMyAccountTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 10, 26);
	private static final BookEntity BOOK = FakeCatalog.book("Clean Architecture", BookEntity.STATUS_ACTIVE);
	private static final UUID UNKNOWN_BOOK = UUID.randomUUID();
	private static final BigDecimal RATE = new BigDecimal("1000.00");

	@Test
	@DisplayName("el panel muestra cada préstamo, multa, devolución y pago con el título del libro, y las reservas")
	void showsAccountWithTitles() {
		final FakeCatalog.Books books = new FakeCatalog.Books().with(BOOK);

		final var response = controller(books).view();
		final MyAccountResponseDTO account = response.getBody();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Clean Architecture", account.activeLoans().get(0).book().title());
		assertEquals("Robert C. Martin", account.activeLoans().get(0).book().authors());
		assertEquals(new BigDecimal("3000.00"), account.activeLoans().get(0).accruedFine());
		assertEquals("Clean Architecture", account.pendingFines().get(0).book().title());
		assertEquals(new BigDecimal("5000.00"), account.pendingFinesTotal());
		assertEquals(new BigDecimal("3000.00"), account.accruingFinesTotal());
		assertEquals("", account.loanHistory().get(0).book().title());
		assertEquals(UNKNOWN_BOOK, account.loanHistory().get(0).book().id());
		assertEquals("uco-1", account.payments().get(0).reference());
		assertEquals(2, account.reservations().get(0).position());
		assertEquals(List.of(BOOK.id(), UNKNOWN_BOOK), books.searches().get(0).getIds());
	}

	@Test
	@DisplayName("sin préstamos ni multas no busca libros")
	void emptyAccountSkipsBooks() {
		final FakeCatalog.Books books = new FakeCatalog.Books();
		final ViewMyAccountUseCaseImpl useCase = new ViewMyAccountUseCaseImpl(
				() -> new MyAccountDomain(null, null, null, null, null, null, null), List::of, books);

		final MyAccountDomain account = useCase.execute();

		assertTrue(books.searches().isEmpty());
		assertEquals(BigDecimal.ZERO, account.pendingFinesTotal());
		assertTrue(account.books().isEmpty());
	}

	@Test
	@DisplayName("no se crea sin sus dependencias")
	void requiresDependencies() {
		final FakeCatalog.Books books = new FakeCatalog.Books();

		assertThrows(BookHiveException.class, () -> new ViewMyAccountUseCaseImpl(null, List::of, books));
	}

	private static MyAccountController controller(final FakeCatalog.Books books) {
		final CirculationUserAccountAdapter circulation = new CirculationUserAccountAdapter(() -> new UserAccountResponseDTO(
				List.of(new UserAccountResponseDTO.Loan(UUID.randomUUID(), BOOK.id(), TODAY.minusDays(20), TODAY.minusDays(3), 3,
						new BigDecimal("3000.00"))),
				List.of(new UserAccountResponseDTO.Fine(UUID.randomUUID(), BOOK.id(), TODAY.minusDays(9), 5, RATE,
						new BigDecimal("5000.00"))),
				new BigDecimal("5000.00"),
				new BigDecimal("3000.00"),
				List.of(new UserAccountResponseDTO.Return(UUID.randomUUID(), UNKNOWN_BOOK, TODAY.minusDays(40),
						TODAY.minusDays(25), TODAY.minusDays(24), 1)),
				List.of(new UserAccountResponseDTO.Payment(UUID.randomUUID(), BOOK.id(), "uco-1", new BigDecimal("1000.00"),
						Instant.parse("2026-10-20T15:00:00Z")))));
		final MyReservationDomain reservation = new MyReservationDomain(UUID.randomUUID(), BOOK.id(), BOOK.title(), "",
				"EN_ESPERA", 2, Instant.parse("2026-10-25T15:00:00Z"), null);

		return new MyAccountController(new ViewMyAccountInputAdapter(
				new ViewMyAccountUseCaseImpl(circulation, () -> List.of(reservation), books)));
	}

}
