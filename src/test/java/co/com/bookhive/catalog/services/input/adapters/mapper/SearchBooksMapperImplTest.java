package co.com.bookhive.catalog.services.input.adapters.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.domain.book.searchbooks.FacetValueDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBookDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.SearchBooksDomain;
import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FoundBookDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SearchBooksMapperImplTest {

	private final SearchBooksMapperImpl mapper = new SearchBooksMapperImpl();

	@Test
	@DisplayName("convierte la petición en el dominio de la búsqueda")
	void mapsRequestToDomain() {
		final SearchBooksDomain domain = mapper.toDomain(new SearchBooksRequestDTO(" clean ", "Martin", "Software", "Pearson", 3));

		assertEquals(new SearchBooksDomain("clean", "Martin", "Software", "Pearson", 3), domain);
	}

	@Test
	@DisplayName("sin petición busca todo desde la primera página")
	void nullRequestSearchesEverything() {
		assertEquals(new SearchBooksDomain("", "", "", "", 1), mapper.toDomain(null));
	}

	@Test
	@DisplayName("convierte los libros encontrados, sus ejemplares y las facetas en la respuesta")
	void mapsDomainToResponse() {
		final UUID id = UUID.randomUUID();
		final FoundBooksDomain domain = new FoundBooksDomain(
				1,
				1,
				List.of(new FoundBookDomain(id, "Clean Code", List.of("Robert C. Martin"), "Software", "Pearson",
						"FISICO", 2008, "978-0132350884", 2, 4)),
				List.of(new FacetValueDomain("Robert C. Martin", 1)),
				List.of(new FacetValueDomain("Software", 1)),
				List.of(new FacetValueDomain("Pearson", 1)));

		final SearchBooksResponseDTO response = mapper.toResponse(domain);

		assertEquals(1, response.found());
		assertEquals(1, response.page());
		assertEquals(new FoundBookDTO(id, "Clean Code", List.of("Robert C. Martin"), "Software", "Pearson",
				"FISICO", 2008, "978-0132350884", 2, 4), response.books().get(0));
		assertEquals("Robert C. Martin", response.authors().get(0).value());
		assertEquals("Software", response.categories().get(0).value());
		assertEquals(1, response.publishers().get(0).count());
	}

	@Test
	@DisplayName("si el caso de uso no devuelve nada lo reporta como error inesperado")
	void rejectsNullDomain() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> mapper.toResponse(null));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
	}

}
