package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import co.com.bookhive.circulation.services.input.dto.request.ReturnBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReturnBookResponseDTO;
import co.com.bookhive.crosscutting.helper.DateHelper;

class ReturnControllerTest {

	@Test
	@DisplayName("responde 201 con el detalle de la devolución")
	void respondsCreated() {
		final UUID bookCopy = UUID.randomUUID();
		final ReturnBookResponseDTO answer = new ReturnBookResponseDTO(UUID.randomUUID(), bookCopy,
				DateHelper.todayInUtc(), DateHelper.todayInUtc(), 0, false, null, BigDecimal.ZERO, BigDecimal.ZERO);
		final ReturnController controller = new ReturnController(request -> answer);

		final ResponseEntity<ReturnBookResponseDTO> response = controller.returnBook(new ReturnBookRequestDTO(bookCopy));

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(answer, response.getBody());
	}

}
