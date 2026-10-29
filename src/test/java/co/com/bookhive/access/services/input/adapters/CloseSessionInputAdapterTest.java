package co.com.bookhive.access.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.services.input.dto.request.CloseSessionRequestDTO;

class CloseSessionInputAdapterTest {

	private final List<String> closedSessions = new ArrayList<>();

	private final CloseSessionInputAdapter adapter =
			new CloseSessionInputAdapter(session -> closedSessions.add(session.sessionToken()));

	@Test
	@DisplayName("le pasa al caso de uso la cookie que llegó")
	void passesSessionToUseCase() {
		adapter.execute(new CloseSessionRequestDTO(" cookie-de-sesion "));
		adapter.execute(null);

		assertEquals(List.of("cookie-de-sesion", ""), closedSessions);
	}

}
