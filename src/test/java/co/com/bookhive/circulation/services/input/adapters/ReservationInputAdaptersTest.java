package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelReservationDomain;
import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelledReservationDomain;
import co.com.bookhive.circulation.domain.reservation.finduserreservations.UserReservationDomain;
import co.com.bookhive.circulation.domain.reservation.reservebook.ReserveBookDomain;
import co.com.bookhive.circulation.domain.reservation.reservebook.ReservedBookDomain;
import co.com.bookhive.circulation.services.input.dto.request.CancelReservationRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.ReserveBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.CancelReservationResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReserveBookResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserReservationResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ReservationInputAdaptersTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID RESERVATION = UUID.randomUUID();

	@Test
	@DisplayName("reservar arma el dominio sin usuario y devuelve el estado, el puesto y el plazo para recoger")
	void reserveMapsRequestAndResult() {
		final LocalDate deadline = LocalDate.of(2026, 10, 29);
		final List<ReserveBookDomain> received = new ArrayList<>();
		final ReserveBookInputAdapter adapter = new ReserveBookInputAdapter(domain -> {
			received.add(domain);
			return new ReservedBookDomain(RESERVATION, domain.getBook(), "ASIGNADA", 0, deadline);
		});

		final ReserveBookResponseDTO response = adapter.execute(new ReserveBookRequestDTO(BOOK));

		assertEquals(new ReserveBookResponseDTO(RESERVATION, BOOK, "ASIGNADA", 0, deadline), response);
		assertEquals(BOOK, received.get(0).getBook());
		assertEquals(UUIDHelper.getDefaultValue(), received.get(0).getUser());
	}

	@Test
	@DisplayName("reservar sin petición o sin respuesta del caso de uso se rechaza")
	void reserveRejectsMissingData() {
		final ReserveBookInputAdapter withoutAnswer = new ReserveBookInputAdapter(domain -> null);
		final ReserveBookRequestDTO request = new ReserveBookRequestDTO(BOOK);

		assertEquals(MessageKey.RESERVATION_BOOK_REQUIRED,
				assertThrows(BookHiveException.class, () -> withoutAnswer.execute(null)).getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> withoutAnswer.execute(request)).getKind());
	}

	@Test
	@DisplayName("cancelar arma el dominio con la reserva y devuelve el estado nuevo")
	void cancelMapsRequestAndResult() {
		final List<CancelReservationDomain> received = new ArrayList<>();
		final CancelReservationInputAdapter adapter = new CancelReservationInputAdapter(domain -> {
			received.add(domain);
			return new CancelledReservationDomain(domain.getReservation(), BOOK, "CANCELADA");
		});

		final CancelReservationResponseDTO response = adapter.execute(new CancelReservationRequestDTO(RESERVATION));

		assertEquals(new CancelReservationResponseDTO(RESERVATION, BOOK, "CANCELADA"), response);
		assertEquals(UUIDHelper.getDefaultValue(), received.get(0).getUser());
	}

	@Test
	@DisplayName("cancelar sin petición o sin respuesta del caso de uso se rechaza")
	void cancelRejectsMissingData() {
		final CancelReservationInputAdapter withoutAnswer = new CancelReservationInputAdapter(domain -> null);
		final CancelReservationRequestDTO request = new CancelReservationRequestDTO(RESERVATION);

		assertEquals(MessageKey.RESERVATION_ID_REQUIRED,
				assertThrows(BookHiveException.class, () -> withoutAnswer.execute(null)).getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> withoutAnswer.execute(request)).getKind());
	}

	@Test
	@DisplayName("las reservas del usuario pasan tal cual a la respuesta")
	void findMapsEveryReservation() {
		final Instant requestedAt = Instant.parse("2026-10-20T10:00:00Z");
		final LocalDate deadline = LocalDate.of(2026, 10, 26);
		final FindUserReservationsInputAdapter adapter = new FindUserReservationsInputAdapter(
				() -> List.of(new UserReservationDomain(RESERVATION, BOOK, "ASIGNADA", 0, requestedAt, deadline)));

		assertEquals(List.of(new UserReservationResponseDTO(RESERVATION, BOOK, "ASIGNADA", 0, requestedAt, deadline)),
				adapter.execute());
	}

	@Test
	@DisplayName("los registros de reservas convierten los nulos y los puestos negativos en valores por defecto")
	void recordsReplaceNulls() {
		final UserReservationResponseDTO empty = new UserReservationResponseDTO(null, null, null, -2, null, null);
		final ReserveBookResponseDTO reserved = new ReserveBookResponseDTO(null, null, null, -1, null);

		assertEquals(UUIDHelper.getDefaultValue(), empty.reservation());
		assertEquals("", empty.status());
		assertEquals(0, empty.position());
		assertEquals(0, reserved.position());
		assertEquals(DateHelper.getDefaultValue(), reserved.pickupDeadline());
		assertEquals(UUIDHelper.getDefaultValue(), new CancelReservationRequestDTO(null).reservation());
		assertEquals(UUIDHelper.getDefaultValue(), new ReserveBookRequestDTO(null).book());
		assertEquals("", new CancelReservationResponseDTO(null, null, null).status());
		assertEquals(0, new ReservedBookDomain(null, null, null, -5, null).position());
		assertEquals(DateHelper.getDefaultValue(), new ReservedBookDomain(null, null, null, 0, null).pickupDeadline());
		assertEquals("", new CancelledReservationDomain(null, null, null).status());
		assertEquals(0, new UserReservationDomain(null, null, null, -1, null, null).position());
	}

}
