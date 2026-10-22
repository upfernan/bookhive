package co.com.bookhive.catalog.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.SearchBooksDomain;
import co.com.bookhive.catalog.domain.catalog.syncallcatalogs.SyncAllCatalogsDomain;
import co.com.bookhive.catalog.domain.catalog.synccatalog.SyncCatalogDomain;
import co.com.bookhive.catalog.services.input.adapters.mapper.SearchBooksMapperImpl;
import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.catalog.services.input.dto.response.SyncAllCatalogsResponseDTO;
import co.com.bookhive.catalog.services.input.dto.response.SyncCatalogResponseDTO;

class CatalogInputAdaptersTest {

	@Test
	@DisplayName("la búsqueda pasa por el mapeador, el caso de uso y vuelve como respuesta")
	void searchGoesThroughUseCase() {
		final List<SearchBooksDomain> received = new ArrayList<>();
		final SearchBooksInputAdapter adapter = new SearchBooksInputAdapter(search -> {
			received.add(search);
			return new FoundBooksDomain(7, search.page(), List.of(), List.of(), List.of(), List.of());
		}, new SearchBooksMapperImpl());

		final SearchBooksResponseDTO response = adapter.execute(new SearchBooksRequestDTO("clean", "", "", "", 2));

		assertEquals(List.of(new SearchBooksDomain("clean", "", "", "", 2)), received);
		assertEquals(7, response.found());
		assertEquals(2, response.page());
	}

	@Test
	@DisplayName("la sincronización de la institución devuelve su resumen")
	void syncReturnsSummary() {
		final SyncCatalogResponseDTO response =
				new SyncCatalogInputAdapter(() -> new SyncCatalogDomain(3, 1, true)).execute();

		assertEquals(new SyncCatalogResponseDTO(3, 1, true), response);
	}

	@Test
	@DisplayName("la sincronización de todas las instituciones devuelve cuáles quedaron y cuáles fallaron")
	void syncAllReturnsSummary() {
		final SyncAllCatalogsResponseDTO response = new SyncAllCatalogsInputAdapter(
				() -> new SyncAllCatalogsDomain(List.of("uco"), List.of("unal: caído"))).execute();

		assertEquals(List.of("uco"), response.synced());
		assertEquals(List.of("unal: caído"), response.failed());
	}

}
