package co.com.bookhive.circulation.services.usecase.reservation.expirereservations.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.reservation.expirereservations.ExpiredReservationsDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandOverBookCopyDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandedOverBookCopyDomain;
import co.com.bookhive.circulation.services.input.adapters.ExpireReservationsInputAdapter;
import co.com.bookhive.circulation.services.input.dto.request.ExpireReservationsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ExpiredReservationsResponseDTO;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class ExpireReservationsUseCaseImplTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID LATE_COPY = UUID.randomUUID();
	private static final UUID ON_TIME_COPY = UUID.randomUUID();

	private FakeRepositories.Reservations reservations;
	private FakeRepositories.BookCopies bookCopies;
	private FakeRepositories.Parameters parameters;
	private List<HandOverBookCopyDomain> handOvers;
	private ReservationEntity late;
	private ReservationEntity onTime;

	@BeforeEach
	void prepare() {
		parameters = new FakeRepositories.Parameters().with(15, 3);
		final LocalDate today = CurrentInstitution.today(CurrentInstitution.parameters(parameters));
		late = assigned(LATE_COPY, today.minusDays(1));
		onTime = assigned(ON_TIME_COPY, today);
		reservations = new FakeRepositories.Reservations().with(late).with(onTime);
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(LATE_COPY, BookCopyEntity.STATUS_RESERVED, true))
				.with(FakeRepositories.bookCopy(ON_TIME_COPY, BookCopyEntity.STATUS_RESERVED, true));
		handOvers = new ArrayList<>();
	}

	@Test
	@DisplayName("vence las reservas cuyo plazo ya pasó y ofrece su ejemplar al siguiente de la fila")
	void expiresLateReservationsAndHandsCopyOver() {
		final ExpiredReservationsDomain result = useCase(nextInQueue()).execute();

		assertEquals(new ExpiredReservationsDomain(1, 1), result);
		assertEquals(ReservationEntity.STATUS_EXPIRED, late.getStatus());
		assertEquals(ReservationEntity.STATUS_ASSIGNED, onTime.getStatus());
		assertEquals(List.of(LATE_COPY + ":RESERVADO->DISPONIBLE"), bookCopies.statusChanges());
		assertEquals(List.of(new HandOverBookCopyDomain(LATE_COPY, BOOK)), handOvers);
	}

	@Test
	@DisplayName("si no hay nadie más en la fila, el ejemplar vuelve a quedar disponible")
	void withoutQueueCopyBecomesAvailable() {
		final ExpiredReservationsDomain result = useCase(nobody()).execute();

		assertEquals(new ExpiredReservationsDomain(1, 0), result);
		assertEquals(List.of(LATE_COPY + ":RESERVADO->DISPONIBLE"), bookCopies.statusChanges());
	}

	@Test
	@DisplayName("si el ejemplar ya no estaba apartado, vence la reserva pero no lo ofrece a nadie")
	void copyNoLongerReservedIsNotHandedOver() {
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(LATE_COPY, BookCopyEntity.STATUS_LENT, true));

		final ExpiredReservationsDomain result = useCase(nextInQueue()).execute();

		assertEquals(new ExpiredReservationsDomain(1, 0), result);
		assertTrue(handOvers.isEmpty());
	}

	@Test
	@DisplayName("si el lector la retiró justo a tiempo, la reserva no se vence")
	void pickedUpMeanwhileIsNotExpired() {
		reservations.changedByOtherRequestTo(ReservationEntity.STATUS_PICKED_UP);

		final ExpiredReservationsDomain result = useCase(nextInQueue()).execute();

		assertEquals(new ExpiredReservationsDomain(0, 0), result);
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(handOvers.isEmpty());
	}

	@Test
	@DisplayName("el adaptador entrega el conteo y el caso de uso no se crea sin dependencias")
	void adapterAndDependencies() {
		final ExpiredReservationsResponseDTO response = new ExpireReservationsInputAdapter(
				() -> new ExpiredReservationsDomain(4, 2)).execute(new ExpireReservationsRequestDTO());

		assertEquals(new ExpiredReservationsResponseDTO(4, 2), response);
		assertEquals(new ExpiredReservationsDomain(0, 0), new ExpiredReservationsDomain(-1, -3));
		assertEquals(new ExpiredReservationsResponseDTO(0, 0), new ExpiredReservationsResponseDTO(-1, -3));
		assertThrows(BookHiveException.class,
				() -> new ExpireReservationsUseCaseImpl(reservations, bookCopies, parameters, null));
	}

	private ExpireReservationsUseCaseImpl useCase(final HandedOverBookCopyDomain answer) {
		return new ExpireReservationsUseCaseImpl(reservations, bookCopies, parameters, domain -> {
			handOvers.add(domain);
			return answer;
		});
	}

	private static HandedOverBookCopyDomain nextInQueue() {
		return new HandedOverBookCopyDomain(UUID.randomUUID(), UUID.randomUUID(), "Luis Mora", LocalDate.now());
	}

	private static HandedOverBookCopyDomain nobody() {
		return HandedOverBookCopyDomain.nobody();
	}

	private static ReservationEntity assigned(final UUID bookCopy, final LocalDate deadline) {
		final ReservationEntity reservation = FakeRepositories.reservation(
				BOOK, UUID.randomUUID(), ReservationEntity.STATUS_ASSIGNED, Instant.parse("2026-10-20T10:00:00Z"));
		reservation.setBookCopy(bookCopy);
		reservation.setPickupDeadline(deadline);
		return reservation;
	}

}
