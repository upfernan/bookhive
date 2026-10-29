package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class ReservationEntityTest {

	@Test
	@DisplayName("nace en espera y sin ejemplar asignado")
	void startsWaiting() {
		final ReservationEntity reservation = new ReservationEntity();

		assertTrue(reservation.isWaiting());
		assertEquals(UUIDHelper.getDefaultValue(), reservation.getBookCopy());
		assertEquals(DateHelper.getDefaultTimestamp(), reservation.getRequestedAt());
		assertEquals(DateHelper.getDefaultTimestamp(), reservation.getAssignedAt());
		assertEquals(DateHelper.getDefaultValue(), reservation.getPickupDeadline());
	}

	@Test
	@DisplayName("guarda los valores y limpia el estado")
	void keepsValues() {
		final UUID bookCopy = UUID.randomUUID();
		final Instant assignedAt = Instant.parse("2026-10-22T15:00:00Z");
		final ReservationEntity reservation = new ReservationEntity();

		reservation.setStatus(" " + ReservationEntity.STATUS_ASSIGNED + " ");
		reservation.setBookCopy(bookCopy);
		reservation.setAssignedAt(assignedAt);
		reservation.setPickupDeadline(LocalDate.of(2026, 10, 24));

		assertFalse(reservation.isWaiting());
		assertEquals(ReservationEntity.STATUS_ASSIGNED, reservation.getStatus());
		assertEquals(bookCopy, reservation.getBookCopy());
		assertEquals(assignedAt, reservation.getAssignedAt());
		assertEquals(LocalDate.of(2026, 10, 24), reservation.getPickupDeadline());

		reservation.setStatus(null);
		assertTrue(reservation.isWaiting());
	}

	@Test
	@DisplayName("los criterios dicen por qué filtran y cuándo están completos")
	void criteriaDescribeTheirFilters() {
		final UUID user = UUID.randomUUID();
		final UUID book = UUID.randomUUID();

		final ReservationCriteria mine = ReservationCriteria.activeOfUser(user);
		final ReservationCriteria queue = ReservationCriteria.waitingForBook(book);
		final ReservationCriteria both = ReservationCriteria.activeOfUserForBook(user, book);

		assertTrue(mine.filtersByUser());
		assertFalse(mine.filtersByBook());
		assertEquals(List.of(ReservationEntity.STATUS_WAITING, ReservationEntity.STATUS_ASSIGNED), mine.getStatuses());
		assertEquals(user, mine.getUser());
		assertFalse(queue.filtersByUser());
		assertEquals(book, queue.getBook());
		assertEquals(List.of(ReservationEntity.STATUS_WAITING), queue.getStatuses());
		assertTrue(both.isComplete());
		assertFalse(ReservationCriteria.activeOfUser(null).isComplete());
	}

	@Test
	@DisplayName("los criterios por ejemplar y por plazo vencido solo buscan reservas asignadas")
	void assignedCriteria() {
		final UUID bookCopy = UUID.randomUUID();
		final LocalDate today = LocalDate.of(2026, 10, 24);

		final ReservationCriteria holding = ReservationCriteria.assignedToBookCopy(bookCopy);
		final ReservationCriteria late = ReservationCriteria.assignedWithPickupBefore(today);

		assertTrue(holding.filtersByBookCopy());
		assertEquals(bookCopy, holding.getBookCopy());
		assertFalse(holding.filtersByPickupBefore());
		assertEquals(List.of(ReservationEntity.STATUS_ASSIGNED), holding.getStatuses());
		assertTrue(late.filtersByPickupBefore());
		assertEquals(today, late.getPickupBefore());
		assertTrue(late.isComplete());
		assertFalse(ReservationCriteria.assignedToBookCopy(null).isComplete());
		assertFalse(ReservationCriteria.assignedWithPickupBefore(null).isComplete());
	}

}
