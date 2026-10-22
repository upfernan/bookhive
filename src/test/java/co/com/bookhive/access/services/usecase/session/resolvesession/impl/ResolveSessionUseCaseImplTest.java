package co.com.bookhive.access.services.usecase.session.resolvesession.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.testdata.FakeAccess;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ResolveSessionUseCaseImplTest {

	private static final UUID PERSON = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	@Test
	@DisplayName("la sesión es la persona activa dueña del pase, con su institución")
	void resolvesActivePerson() {
		final FakeAccess.People people = new FakeAccess.People().with(FakeAccess.person(PERSON, INSTITUTION));

		final ResolveSessionDomain session = new ResolveSessionUseCaseImpl(() -> "uid", people).execute();

		assertEquals(PERSON, session.person());
		assertEquals(INSTITUTION, session.institution());
		assertEquals("uid", people.searches().get(0).getIdentity());
	}

	@Test
	@DisplayName("si la persona no está activa no hay sesión")
	void rejectsUnknownPerson() {
		final ResolveSessionUseCaseImpl useCase = new ResolveSessionUseCaseImpl(() -> "uid", new FakeAccess.People());

		final BookHiveException exception = assertThrows(BookHiveException.class, useCase::execute);

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("si el pase no trae persona responde con un error inesperado")
	void rejectsPassWithoutIdentity() {
		final ResolveSessionUseCaseImpl useCase = new ResolveSessionUseCaseImpl(() -> " ", new FakeAccess.People());

		final BookHiveException exception = assertThrows(BookHiveException.class, useCase::execute);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

}
