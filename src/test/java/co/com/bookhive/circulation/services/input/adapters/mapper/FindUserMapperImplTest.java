package co.com.bookhive.circulation.services.input.adapters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.ReadyReservationDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReadyReservationResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindUserMapperImplTest {

	private final FindUserMapperImpl mapper = new FindUserMapperImpl();

	@Test
	@DisplayName("lleva el correo de la petición al dominio sin espacios")
	void mapsRequestToDomain() {
		assertEquals("ana.gomez@uco.edu.co",
				mapper.toDomain(new FindUserRequestDTO("  ana.gomez@uco.edu.co ")).getEmail());
	}

	@Test
	@DisplayName("una petición nula se vuelve una búsqueda vacía, que el dominio rechaza")
	void mapsNullRequestToEmptySearch() {
		assertEquals("", mapper.toDomain(null).getEmail());
	}

	@Test
	@DisplayName("copia en la respuesta todos los datos del usuario encontrado y sus reservas listas")
	void mapsFoundUserToResponse() {
		final UUID user = UUIDHelper.generateNew();
		final UUID reservation = UUIDHelper.generateNew();
		final UUID book = UUIDHelper.generateNew();
		final UUID bookCopy = UUIDHelper.generateNew();
		final LocalDate deadline = LocalDate.of(2026, 10, 29);

		final FindUserResponseDTO response = mapper.toResponse(new FoundUserDomain(
				user, "Ana Gómez", "ana.gomez@uco.edu.co", 1, 3, 2,
				List.of(MessageKey.USER_WITH_PENDING_FINES),
				List.of(new ReadyReservationDomain(reservation, book, bookCopy, "BH-0001", deadline))));

		assertEquals(new FindUserResponseDTO(user, "Ana Gómez", "ana.gomez@uco.edu.co", 1, 3, 2,
				List.of(MessageKey.USER_WITH_PENDING_FINES),
				List.of(new ReadyReservationResponseDTO(reservation, book, bookCopy, "BH-0001", deadline))), response);
	}

	@Test
	@DisplayName("las reservas listas vacías o con nulos quedan con valores por defecto")
	void readyReservationsReplaceNulls() {
		final ReadyReservationResponseDTO empty = new ReadyReservationResponseDTO(null, null, null, null, null);

		assertEquals(UUIDHelper.getDefaultValue(), empty.bookCopy());
		assertEquals("", empty.barcode());
		assertEquals(DateHelper.getDefaultValue(), empty.pickupDeadline());
		assertEquals("", new ReadyReservationDomain(null, null, null, null, null).barcode());
		assertEquals(List.of(), new FoundUserDomain(null, null, null, 0, 0, 0, null, null).readyReservations());
	}

	@Test
	@DisplayName("si el caso de uso no devuelve nada lo trata como error inesperado")
	void rejectsNullResult() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> mapper.toResponse(null));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

}
