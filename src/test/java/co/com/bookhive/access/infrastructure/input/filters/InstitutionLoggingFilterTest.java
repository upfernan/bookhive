package co.com.bookhive.access.infrastructure.input.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class InstitutionLoggingFilterTest {

	@Test
	@DisplayName("marca los registros de la petición y al terminar quita la marca")
	void marksLogsDuringRequest() throws Exception {
		final AtomicReference<String> seen = new AtomicReference<>();

		new InstitutionLoggingFilter().doFilter(
				new MockHttpServletRequest(),
				new MockHttpServletResponse(),
				(request, response) -> seen.set(MDC.get(InstitutionLoggingFilter.MDC_KEY)));

		assertEquals(InstitutionLoggingFilter.UNRESOLVED, seen.get());
		assertNull(MDC.get(InstitutionLoggingFilter.MDC_KEY));
	}

}
