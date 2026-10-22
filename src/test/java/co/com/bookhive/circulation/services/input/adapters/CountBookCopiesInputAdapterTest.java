package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.BookCopiesCountDomain;
import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.CountBookCopiesDomain;
import co.com.bookhive.circulation.services.input.dto.request.CountBookCopiesRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.BookCopiesDTO;
import co.com.bookhive.circulation.services.input.dto.response.CountBookCopiesResponseDTO;

class CountBookCopiesInputAdapterTest {

	@Test
	@DisplayName("convierte la petición en dominio y el conteo en respuesta")
	void mapsThroughUseCase() {
		final UUID book = UUID.randomUUID();
		final List<CountBookCopiesDomain> received = new ArrayList<>();
		final CountBookCopiesInputAdapter adapter = new CountBookCopiesInputAdapter(domain -> {
			received.add(domain);
			return List.of(new BookCopiesCountDomain(book, 1, 3));
		});

		final CountBookCopiesResponseDTO response = adapter.execute(new CountBookCopiesRequestDTO(List.of(book)));

		assertEquals(List.of(new CountBookCopiesDomain(List.of(book))), received);
		assertEquals(List.of(new BookCopiesDTO(book, 1, 3)), response.books());
	}

	@Test
	@DisplayName("sin petición consulta una lista vacía")
	void nullRequestIsEmpty() {
		final List<CountBookCopiesDomain> received = new ArrayList<>();
		final CountBookCopiesInputAdapter adapter = new CountBookCopiesInputAdapter(domain -> {
			received.add(domain);
			return List.of();
		});

		final CountBookCopiesResponseDTO response = adapter.execute(null);

		assertEquals(List.of(), received.get(0).books());
		assertEquals(List.of(), response.books());
	}

	@Test
	@DisplayName("los DTO del conteo nunca quedan con datos nulos")
	void dtosHaveSafeDefaults() {
		assertEquals(List.of(), new CountBookCopiesRequestDTO(null).books());
		assertEquals(List.of(), new CountBookCopiesResponseDTO(null).books());
		assertEquals(new UUID(0, 0), new BookCopiesDTO(null, 0, 0).book());
	}

}
