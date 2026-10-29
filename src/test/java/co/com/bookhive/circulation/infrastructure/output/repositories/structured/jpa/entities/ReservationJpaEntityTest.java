package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class ReservationJpaEntityTest {

	@Test
	@DisplayName("nace sin ejemplar ni fechas y nunca devuelve nulos")
	void startsWithDefaults() {
		final ReservationJpaEntity reservation = new ReservationJpaEntity();

		assertEquals(UUIDHelper.getDefaultValue(), reservation.getId());
		assertEquals(UUIDHelper.getDefaultValue(), reservation.getBook());
		assertEquals(UUIDHelper.getDefaultValue(), reservation.getUser());
		assertEquals("", reservation.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), reservation.getRequestedAt());
		assertEquals(UUIDHelper.getDefaultValue(), reservation.getBookCopy());
		assertEquals(DateHelper.getDefaultTimestamp(), reservation.getAssignedAt());
		assertEquals(DateHelper.getDefaultValue(), reservation.getPickupDeadline());
	}

	@Test
	@DisplayName("guarda los valores de la reserva")
	void keepsValues() {
		final UUID id = UUID.randomUUID();
		final UUID book = UUID.randomUUID();
		final UUID user = UUID.randomUUID();
		final UUID bookCopy = UUID.randomUUID();
		final Instant requestedAt = Instant.parse("2026-10-20T10:00:00Z");
		final Instant assignedAt = Instant.parse("2026-10-22T15:00:00Z");
		final LocalDate deadline = LocalDate.of(2026, 10, 24);
		final ReservationJpaEntity reservation = new ReservationJpaEntity();

		reservation.setId(id);
		reservation.setBook(book);
		reservation.setUser(user);
		reservation.setStatus("ASIGNADA");
		reservation.setRequestedAt(requestedAt);
		reservation.setBookCopy(bookCopy);
		reservation.setAssignedAt(assignedAt);
		reservation.setPickupDeadline(deadline);

		assertEquals(id, reservation.getId());
		assertEquals(book, reservation.getBook());
		assertEquals(user, reservation.getUser());
		assertEquals("ASIGNADA", reservation.getStatus());
		assertEquals(requestedAt, reservation.getRequestedAt());
		assertEquals(bookCopy, reservation.getBookCopy());
		assertEquals(assignedAt, reservation.getAssignedAt());
		assertEquals(deadline, reservation.getPickupDeadline());
	}

	@Test
	@DisplayName("los nulos vuelven a los valores por defecto")
	void replacesNulls() {
		final ReservationJpaEntity reservation = new ReservationJpaEntity();
		reservation.setBookCopy(UUID.randomUUID());

		reservation.setId(null);
		reservation.setStatus(null);
		reservation.setRequestedAt(null);
		reservation.setBookCopy(null);
		reservation.setAssignedAt(null);
		reservation.setPickupDeadline(null);

		assertEquals(UUIDHelper.getDefaultValue(), reservation.getId());
		assertEquals("", reservation.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), reservation.getRequestedAt());
		assertEquals(UUIDHelper.getDefaultValue(), reservation.getBookCopy());
		assertEquals(DateHelper.getDefaultTimestamp(), reservation.getAssignedAt());
		assertEquals(DateHelper.getDefaultValue(), reservation.getPickupDeadline());
	}

}
