package co.com.bookhive.circulation.services.input.adapters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyMapperImplTest {

	private final FindBookCopyMapperImpl mapper = new FindBookCopyMapperImpl();

	@Test
	@DisplayName("lleva el código de la petición al dominio sin espacios")
	void mapsRequestToDomain() {
		assertEquals("UCO-0001", mapper.toDomain(new FindBookCopyRequestDTO("  UCO-0001 ")).getBarcode());
	}

	@Test
	@DisplayName("una petición nula se vuelve una búsqueda vacía, que el dominio rechaza")
	void mapsNullRequestToEmptySearch() {
		assertEquals("", mapper.toDomain(null).getBarcode());
	}

	@Test
	@DisplayName("copia en la respuesta todos los datos del ejemplar encontrado")
	void mapsFoundBookCopyToResponse() {
		final UUID bookCopy = UUIDHelper.generateNew();
		final UUID book = UUIDHelper.generateNew();

		final FindBookCopyResponseDTO response = mapper.toResponse(new FoundBookCopyDomain(
				bookCopy, book, "UCO-0001", false, true, List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE)));

		assertEquals(new FindBookCopyResponseDTO(bookCopy, book, "UCO-0001", false, true,
				List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE)), response);
	}

	@Test
	@DisplayName("si el caso de uso no devuelve nada lo trata como error inesperado")
	void rejectsNullResult() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> mapper.toResponse(null));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

}
