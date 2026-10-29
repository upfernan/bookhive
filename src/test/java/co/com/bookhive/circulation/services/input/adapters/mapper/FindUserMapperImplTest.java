package co.com.bookhive.circulation.services.input.adapters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.OpenLoanDomain;
import co.com.bookhive.circulation.domain.user.finduser.ReadyReservationDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.OpenLoanResponseDTO;
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
	@DisplayName("copia en la respuesta todos los datos del usuario encontrado, sus reservas listas y sus préstamos abiertos")
	void mapsFoundUserToResponse() {
		final UUID user = UUIDHelper.generateNew();
		final UUID reservation = UUIDHelper.generateNew();
		final UUID book = UUIDHelper.generateNew();
		final UUID bookCopy = UUIDHelper.generateNew();
		final UUID loan = UUIDHelper.generateNew();
		final UUID lentCopy = UUIDHelper.generateNew();
		final LocalDate deadline = LocalDate.of(2026, 10, 29);
		final LocalDate loanDate = LocalDate.of(2026, 10, 1);
		final LocalDate dueDate = LocalDate.of(2026, 10, 16);
		final BigDecimal fine = new BigDecimal("3000");

		final FindUserResponseDTO response = mapper.toResponse(new FoundUserDomain(
				user, "Ana Gómez", "ana.gomez@uco.edu.co", 1, 3, 2,
				List.of(MessageKey.USER_WITH_PENDING_FINES),
				List.of(new ReadyReservationDomain(reservation, book, bookCopy, "BH-0001", deadline)),
				List.of(new OpenLoanDomain(loan, lentCopy, "BH-0007", book, loanDate, dueDate, 2, fine))));

		assertEquals(new FindUserResponseDTO(user, "Ana Gómez", "ana.gomez@uco.edu.co", 1, 3, 2,
				List.of(MessageKey.USER_WITH_PENDING_FINES),
				List.of(new ReadyReservationResponseDTO(reservation, book, bookCopy, "BH-0001", deadline)),
				List.of(new OpenLoanResponseDTO(loan, lentCopy, "BH-0007", book, loanDate, dueDate, 2, fine))), response);
	}

	@Test
	@DisplayName("las reservas listas vacías o con nulos quedan con valores por defecto")
	void readyReservationsReplaceNulls() {
		final ReadyReservationResponseDTO empty = new ReadyReservationResponseDTO(null, null, null, null, null);

		assertEquals(UUIDHelper.getDefaultValue(), empty.bookCopy());
		assertEquals("", empty.barcode());
		assertEquals(DateHelper.getDefaultValue(), empty.pickupDeadline());
		assertEquals("", new ReadyReservationDomain(null, null, null, null, null).barcode());
		assertEquals(List.of(), new FoundUserDomain(null, null, null, 0, 0, 0, null, null, null).readyReservations());
		assertEquals(List.of(), new FoundUserDomain(null, null, null, 0, 0, 0, null, null, null).openLoans());
	}

	@Test
	@DisplayName("los préstamos abiertos con nulos o negativos quedan con valores por defecto")
	void openLoansReplaceNulls() {
		final OpenLoanDomain domain = new OpenLoanDomain(null, null, null, null, null, null, -3, null);
		final OpenLoanResponseDTO response = new OpenLoanResponseDTO(null, null, null, null, null, null, -3, null);

		assertEquals(UUIDHelper.getDefaultValue(), domain.loan());
		assertEquals(UUIDHelper.getDefaultValue(), domain.bookCopy());
		assertEquals("", domain.barcode());
		assertEquals("", response.barcode());
		assertEquals(DateHelper.getDefaultValue(), domain.dueDate());
		assertEquals(0, domain.overdueDays());
		assertEquals(BigDecimal.ZERO, domain.accruedFine());
		assertEquals(UUIDHelper.getDefaultValue(), response.book());
		assertEquals(DateHelper.getDefaultValue(), response.loanDate());
		assertEquals(0, response.overdueDays());
		assertEquals(BigDecimal.ZERO, response.accruedFine());
	}

	@Test
	@DisplayName("si el caso de uso no devuelve nada lo trata como error inesperado")
	void rejectsNullResult() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> mapper.toResponse(null));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

}
