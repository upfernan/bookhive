package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.input.dto.request.OpenSessionRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.OpenSessionResponseDTO;
import co.com.bookhive.circulation.services.output.ports.IdentityProviderPort;
import co.com.bookhive.circulation.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class OpenSessionInteractorTest {

	private static final String ID_TOKEN = "pase-del-proveedor";
	private static final String SESSION = "cookie-de-sesion";
	private static final String IDENTITY = "uid-bibliotecario";

	private FakeIdentityProvider identityProvider;
	private List<String> searchedPeople;

	@BeforeEach
	void prepare() {
		identityProvider = new FakeIdentityProvider();
		searchedPeople = new ArrayList<>();
	}

	@Test
	@DisplayName("abre la sesión con el pase y devuelve la cookie cuando la persona está registrada")
	void opensSessionForRegisteredPerson() {
		final OpenSessionInteractor interactor = new OpenSessionInteractor(identityProvider, registeredPeople());

		final OpenSessionResponseDTO response = interactor.execute(new OpenSessionRequestDTO("  " + ID_TOKEN + "  "));

		assertEquals(SESSION, response.sessionToken());
		assertEquals(ID_TOKEN, identityProvider.receivedIdToken);
		assertEquals(List.of(IDENTITY), searchedPeople);
	}

	@Test
	@DisplayName("no abre la sesión si la persona no está registrada en BookHive")
	void rejectsUnregisteredPerson() {
		final OpenSessionInteractor interactor = new OpenSessionInteractor(identityProvider, identity -> {
			throw BookHiveException.createConflict(MessageKey.SESSION_NOT_RESOLVED, "No está registrada.");
		});
		final OpenSessionRequestDTO request = new OpenSessionRequestDTO(ID_TOKEN);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> interactor.execute(request));

		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("pide el pase cuando llega vacío y no llama al proveedor")
	void requiresIdToken() {
		final OpenSessionInteractor interactor = new OpenSessionInteractor(identityProvider, registeredPeople());
		final OpenSessionRequestDTO request = new OpenSessionRequestDTO();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> interactor.execute(request));

		assertEquals(MessageKey.SESSION_ID_TOKEN_REQUIRED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
		assertEquals("", identityProvider.receivedIdToken);
	}

	@Test
	@DisplayName("pide el pase cuando no llega la petición")
	void requiresRequest() {
		final OpenSessionInteractor interactor = new OpenSessionInteractor(identityProvider, registeredPeople());

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> interactor.execute(null));

		assertEquals(MessageKey.SESSION_ID_TOKEN_REQUIRED, exception.getUserMessageKey());
	}

	private PersonRepository registeredPeople() {
		return identity -> {
			searchedPeople.add(identity);
			return new PersonEntity();
		};
	}

	private static final class FakeIdentityProvider implements IdentityProviderPort {

		private String receivedIdToken = "";

		@Override
		public String openSession(final String idToken) {
			receivedIdToken = idToken;
			return SESSION;
		}

		@Override
		public String identityOf(final String sessionToken) {
			return SESSION.equals(sessionToken) ? IDENTITY : "";
		}

		@Override
		public void closeSession(final String sessionToken) {
			throw new UnsupportedOperationException("Abrir una sesión no la cierra.");
		}

	}

}
