package co.com.bookhive.shared.services.output.ports.repositories.criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageCriteriaTest {

	@Test
	@DisplayName("una página negativa se vuelve la primera")
	void fixesNegativePage() {
		assertEquals(0, PageCriteria.of(-5, 10).getPage());
	}

	@Test
	@DisplayName("el tamaño no pasa del máximo")
	void limitsSize() {
		assertEquals(100, PageCriteria.of(0, 500).getSize());
	}

	@Test
	@DisplayName("un tamaño de cero usa el tamaño por defecto")
	void usesDefaultSize() {
		assertEquals(20, PageCriteria.of(0, 0).getSize());
	}

	@Test
	@DisplayName("la página por defecto es la primera con 20 elementos")
	void buildsDefaultPage() {
		final PageCriteria page = PageCriteria.defaultPage();

		assertEquals(0, page.getPage());
		assertEquals(20, page.getSize());
	}

}
