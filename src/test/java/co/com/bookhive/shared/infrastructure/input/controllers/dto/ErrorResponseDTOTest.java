package co.com.bookhive.shared.infrastructure.input.controllers.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorResponseDTOTest {

	@Test
	@DisplayName("nace con mensaje y traza vacíos, nunca nulos")
	void startsEmpty() {
		final ErrorResponseDTO response = new ErrorResponseDTO();

		assertEquals("", response.getMessage());
		assertEquals("", response.getTraceId());
	}

	@Test
	@DisplayName("guarda el mensaje y la traza sin espacios en los extremos")
	void keepsTrimmedValues() {
		final ErrorResponseDTO response = ErrorResponseDTO.create(" No se pudo prestar. ", " 4bf92f35 ");

		assertEquals("No se pudo prestar.", response.getMessage());
		assertEquals("4bf92f35", response.getTraceId());
	}

	@Test
	@DisplayName("convierte los nulos en texto vacío")
	void replacesNulls() {
		final ErrorResponseDTO response = ErrorResponseDTO.create(null, null);

		assertEquals("", response.getMessage());
		assertEquals("", response.getTraceId());
	}

}
