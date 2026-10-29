package co.com.bookhive.circulation.services.input.adapters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LendBookMapperImplTest {

	private final LendBookMapper mapper = new LendBookMapperImpl();

	@Test
	@DisplayName("arma el dominio con la petición y la fecha de hoy en UTC, sin datos de la sesión")
	void buildsDomain() {
		final UUID bookCopy = UUIDHelper.generateNew();
		final UUID user = UUIDHelper.generateNew();

		final LendBookDomain domain = mapper.toDomain(new LendBookRequestDTO(bookCopy, user));

		assertEquals(bookCopy, domain.getBookCopy());
		assertEquals(user, domain.getUser());
		assertEquals(UUIDHelper.getDefaultValue(), domain.getInstitution());
		assertEquals(UUIDHelper.getDefaultValue(), domain.getLibrarian());
		assertEquals(DateHelper.getDefaultValue(), domain.getLoanDate());
		assertEquals(UUIDHelper.getDefaultValue(), domain.getId());
	}

	@Test
	@DisplayName("rechaza una petición vacía")
	void rejectsNullRequest() {
		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> mapper.toDomain(null));

		assertEquals(MessageKey.LOAN_DATA_REQUIRED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("devuelve el número del préstamo y la fecha de entrega")
	void buildsResponse() {
		final UUID id = UUIDHelper.generateNew();
		final LocalDate dueDate = LocalDate.of(2026, 11, 1);
		final LendBookDomain domain = new LendBookDomain(id, null, null, null, null, null, dueDate);

		final LendBookResponseDTO response = mapper.toResponse(domain);

		assertEquals(id, response.getLoan());
		assertEquals(dueDate, response.getDueDate());
	}

	@Test
	@DisplayName("rechaza una respuesta sin dominio")
	void rejectsNullDomain() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> mapper.toResponse(null));

		assertEquals(MessageKey.LOAN_NOT_CONFIRMED, exception.getUserMessageKey());
	}

}
