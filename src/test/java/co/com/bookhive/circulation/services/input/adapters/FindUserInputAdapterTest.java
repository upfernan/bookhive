package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.user.finduser.FindUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.FindUserMapperImpl;
import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.circulation.services.usecase.user.finduser.FindUserUseCase;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindUserInputAdapterTest {

	private static final UUID USER = UUIDHelper.generateNew();

	private FakeUseCase useCase;
	private FindUserInputAdapter inputAdapter;

	@BeforeEach
	void prepare() {
		useCase = new FakeUseCase();
		inputAdapter = new FindUserInputAdapter(useCase, new FindUserMapperImpl());
	}

	@Test
	@DisplayName("convierte la petición en dominio y se la entrega al caso de uso")
	void buildsDomainFromRequest() {
		inputAdapter.execute(new FindUserRequestDTO("ana.gomez@uco.edu.co"));

		assertEquals("ana.gomez@uco.edu.co", useCase.received.getEmail());
	}

	@Test
	@DisplayName("devuelve lo que encontró el caso de uso, con los bloqueos como claves de mensaje")
	void returnsFoundUser() {
		final FindUserResponseDTO response = inputAdapter.execute(new FindUserRequestDTO("ana.gomez@uco.edu.co"));

		assertEquals(USER, response.id());
		assertEquals("Ana Gómez", response.name());
		assertEquals(List.of(MessageKey.USER_AT_LOAN_LIMIT), response.blocks());
	}

	private static final class FakeUseCase implements FindUserUseCase {

		private FindUserDomain received;

		@Override
		public FoundUserDomain execute(final FindUserDomain input) {
			received = input;
			return new FoundUserDomain(USER, "Ana Gómez", input.getEmail(), 3, 3, 0,
					List.of(MessageKey.USER_AT_LOAN_LIMIT));
		}

	}

}
