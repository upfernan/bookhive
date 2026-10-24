package co.com.bookhive.circulation.services.usecase.reservation.cancelreservation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelReservationDomain;
import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelledReservationDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class CancelReservationUseCaseImplTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID READER = UUID.randomUUID();
	private static final UUID INSTITUTION = UUID.randomUUID();

	private FakeRepositories.Reservations reservations;
	private ReservationEntity waiting;
	private List<String> audited;
	private UUID sessionUser;

	@BeforeEach
	void prepare() {
		waiting = FakeRepositories.reservation(BOOK, READER, ReservationEntity.STATUS_WAITING, Instant.now());
		reservations = new FakeRepositories.Reservations().with(waiting);
		audited = new ArrayList<>();
		sessionUser = READER;
	}

	@Test
	@DisplayName("cancela la reserva propia que está en espera")
	void cancelsOwnWaitingReservation() {
		final CancelledReservationDomain cancelled = useCase().execute(request(waiting.getId()));

		assertEquals(waiting.getId(), cancelled.reservation());
		assertEquals(BOOK, cancelled.book());
		assertEquals(ReservationEntity.STATUS_CANCELLED, cancelled.status());
		assertEquals(ReservationEntity.STATUS_CANCELLED, waiting.getStatus());
		assertEquals(List.of(AuditPort.RESERVATION_CANCELLED + " " + INSTITUTION + " " + READER + " " + waiting.getId()),
				audited);
	}

	@Test
	@DisplayName("la reserva de otro usuario o una que no existe se responden igual: no encontrada")
	void otherUsersReservationIsNotFound() {
		final ReservationEntity foreign =
				FakeRepositories.reservation(BOOK, UUID.randomUUID(), ReservationEntity.STATUS_WAITING, Instant.now());
		reservations.with(foreign);

		assertRejected(foreign.getId(), MessageKey.RESERVATION_NOT_FOUND);
		assertRejected(UUID.randomUUID(), MessageKey.RESERVATION_NOT_FOUND);
		assertEquals(ReservationEntity.STATUS_WAITING, foreign.getStatus());
	}

	@Test
	@DisplayName("una reserva que ya no está en espera no se cancela")
	void onlyWaitingReservationsAreCancelled() {
		waiting.setStatus(ReservationEntity.STATUS_ASSIGNED);

		final BookHiveException exception = assertRejected(waiting.getId(), MessageKey.RESERVATION_NOT_CANCELLABLE);

		assertTrue(exception.getTechnicalMessage().contains(ReservationEntity.STATUS_ASSIGNED));
	}

	@Test
	@DisplayName("si otra petición cambió la reserva entre la lectura y la cancelación, se rechaza con el estado nuevo")
	void concurrentChangeIsRejected() {
		reservations.changedByOtherRequestTo(ReservationEntity.STATUS_ASSIGNED);

		final BookHiveException exception = assertRejected(waiting.getId(), MessageKey.RESERVATION_NOT_CANCELLABLE);

		assertTrue(exception.getTechnicalMessage().contains(ReservationEntity.STATUS_ASSIGNED));
	}

	@Test
	@DisplayName("sin reserva, sin dominio o sin usuario en la sesión no cancela")
	void rejectsMissingData() {
		final CancelReservationUseCaseImpl useCase = useCase();
		final CancelReservationDomain withoutReservation = request(null);

		assertEquals(MessageKey.RESERVATION_ID_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(withoutReservation)).getUserMessageKey());
		assertEquals(MessageKey.RESERVATION_ID_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getUserMessageKey());

		sessionUser = null;
		final CancelReservationUseCaseImpl withoutUser = useCase();
		final CancelReservationDomain request = request(waiting.getId());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> withoutUser.execute(request)).getKind());
		assertEquals(ReservationEntity.STATUS_WAITING, waiting.getStatus());
	}

	@Test
	@DisplayName("el dominio sin repositorio y el caso de uso sin dependencias se rechazan")
	void rejectsMissingPorts() {
		final CancelReservationDomain withoutPorts = request(waiting.getId()).withUser(READER);
		final FakeSession session = new FakeSession();

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, withoutPorts::validate).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, withoutPorts::findCancellable).getKind());
		assertThrows(BookHiveException.class, () -> new CancelReservationUseCaseImpl(null, session, this::audit));
	}

	private BookHiveException assertRejected(final UUID reservation, final String messageKey) {
		final CancelReservationUseCaseImpl useCase = useCase();
		final CancelReservationDomain request = request(reservation);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(messageKey, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertTrue(audited.isEmpty());
		return exception;
	}

	private static CancelReservationDomain request(final UUID reservation) {
		return new CancelReservationDomain(reservation, null);
	}

	private CancelReservationUseCaseImpl useCase() {
		return new CancelReservationUseCaseImpl(reservations, new FakeSession(), this::audit);
	}

	private void audit(final String action, final UUID institution, final UUID person, final String detail) {
		audited.add(action + " " + institution + " " + person + " " + detail);
	}

	private final class FakeSession implements SessionContextPort {

		@Override
		public UUID getInstitution() {
			return INSTITUTION;
		}

		@Override
		public UUID getLibrarian() {
			return sessionUser;
		}

	}

}
