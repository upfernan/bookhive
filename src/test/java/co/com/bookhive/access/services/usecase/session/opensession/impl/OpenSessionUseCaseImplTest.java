package co.com.bookhive.access.services.usecase.session.opensession.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.domain.session.opensession.OpenSessionDomain;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.access.testdata.FakeAccess;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class OpenSessionUseCaseImplTest {

	private static final String IDENTITY = "uid-bibliotecario";

	@Test
	@DisplayName("abre la sesión con el pase y devuelve la cookie cuando la persona está activa")
	void opensSessionForActivePerson() {
		final FakeAccess.IdentityProvider provider = new FakeAccess.IdentityProvider(IDENTITY);
		final FakeAccess.People people = new FakeAccess.People()
				.with(FakeAccess.person(UUIDHelper.generateNew(), UUIDHelper.generateNew()));

		final OpenSessionDomain session =
				new OpenSessionUseCaseImpl(provider, people).execute(new OpenSessionDomain("  pase  ", ""));

		assertEquals("cookie-de-sesion", session.sessionToken());
		assertEquals(List.of("pase"), provider.receivedIdTokens());
		assertEquals(IDENTITY, people.searches().get(0).getIdentity());
		assertEquals(PersonEntity.STATUS_ACTIVE, people.searches().get(0).getStatus());
	}

	@Test
	@DisplayName("no abre la sesión si la persona no está activa en BookHive")
	void rejectsUnknownPerson() {
		final OpenSessionUseCaseImpl useCase =
				new OpenSessionUseCaseImpl(new FakeAccess.IdentityProvider(IDENTITY), new FakeAccess.People());
		final OpenSessionDomain request = new OpenSessionDomain("pase", "");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no abre la sesión sin el pase del proveedor de identidad")
	void rejectsMissingIdToken() {
		final FakeAccess.IdentityProvider provider = new FakeAccess.IdentityProvider(IDENTITY);
		final OpenSessionUseCaseImpl useCase = new OpenSessionUseCaseImpl(provider, new FakeAccess.People());

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.SESSION_ID_TOKEN_REQUIRED, exception.getUserMessageKey());
		assertTrue(provider.receivedIdTokens().isEmpty());
	}

	@Test
	@DisplayName("si el proveedor no dice de quién es el pase responde con un error inesperado")
	void rejectsPassWithoutIdentity() {
		final OpenSessionUseCaseImpl useCase =
				new OpenSessionUseCaseImpl(new FakeAccess.IdentityProvider("  "), new FakeAccess.People());
		final OpenSessionDomain request = new OpenSessionDomain("pase", "");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

}
