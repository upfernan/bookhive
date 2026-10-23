package co.com.bookhive.catalog.services.input.adapters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.services.input.dto.request.FindBookCopyDetailsRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyDetailsMapperImplTest {

	private final FindBookCopyDetailsMapperImpl mapper = new FindBookCopyDetailsMapperImpl();

	@Test
	@DisplayName("lleva el código de la petición al dominio sin espacios")
	void mapsRequestToDomain() {
		assertEquals("UCO-0001", mapper.toDomain(new FindBookCopyDetailsRequestDTO(" UCO-0001 ")).barcode());
	}

	@Test
	@DisplayName("una petición nula se vuelve una búsqueda vacía, que el dominio rechaza")
	void mapsNullRequestToEmptySearch() {
		assertEquals("", mapper.toDomain(null).barcode());
	}

	@Test
	@DisplayName("copia en la respuesta todos los datos del ejemplar")
	void mapsDetailsToResponse() {
		final UUID bookCopy = UUID.randomUUID();

		final FindBookCopyDetailsResponseDTO response = mapper.toResponse(new BookCopyDetailsDomain(
				bookCopy, "UCO-0001", "Clean Architecture", "Robert C. Martin", "", true, false,
				List.of(MessageKey.BOOK_COPY_REFERENCE_ONLY)));

		assertEquals(new FindBookCopyDetailsResponseDTO(bookCopy, "UCO-0001", "Clean Architecture",
				"Robert C. Martin", "", true, false, List.of(MessageKey.BOOK_COPY_REFERENCE_ONLY)), response);
	}

	@Test
	@DisplayName("si el caso de uso no devuelve nada lo trata como error inesperado")
	void rejectsNullResult() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> mapper.toResponse(null));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

}
