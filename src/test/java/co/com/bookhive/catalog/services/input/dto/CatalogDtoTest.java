package co.com.bookhive.catalog.services.input.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FacetValueDTO;
import co.com.bookhive.catalog.services.input.dto.response.FoundBookDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.catalog.services.input.dto.response.SyncAllCatalogsResponseDTO;

class CatalogDtoTest {

	@Test
	@DisplayName("los DTO del catálogo nunca quedan con textos ni listas nulas")
	void dtosHaveSafeDefaults() {
		final SearchBooksRequestDTO request = new SearchBooksRequestDTO(null, null, null, null, 1);
		final FoundBookDTO book = new FoundBookDTO(null, null, null, null, null, null, 0, null, 0, 0);
		final SearchBooksResponseDTO response = new SearchBooksResponseDTO(0, 1, null, null, null, null);
		final SyncAllCatalogsResponseDTO summary = new SyncAllCatalogsResponseDTO(null, null);

		assertEquals("", request.text());
		assertEquals("", request.author());
		assertEquals("", request.category());
		assertEquals("", request.publisher());
		assertEquals(new UUID(0, 0), book.id());
		assertEquals("", book.title());
		assertEquals(List.of(), book.authors());
		assertEquals("", book.category());
		assertEquals("", book.publisher());
		assertEquals("", book.format());
		assertEquals("", book.isbn());
		assertEquals(List.of(), response.books());
		assertEquals(List.of(), response.authors());
		assertEquals(List.of(), response.categories());
		assertEquals(List.of(), response.publishers());
		assertEquals(List.of(), summary.synced());
		assertEquals(List.of(), summary.failed());
		assertEquals("", new FacetValueDTO(null, 1).value());
	}

}
