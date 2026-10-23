package co.com.bookhive.circulation.services.usecase.user.finduser.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.user.finduser.FindUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindUserUseCaseImplTest {

	private static final UUID USER = UUIDHelper.generateNew();
	private static final String EMAIL = "ana.gomez@uco.edu.co";

	private FakeRepositories.Users users;
	private FakeRepositories.Fines fines;
	private FakeRepositories.Loans loans;
	private FakeRepositories.Parameters parameters;

	@BeforeEach
	void prepare() {
		users = new FakeRepositories.Users().with(FakeRepositories.user(USER, "Ana Gómez", EMAIL));
		fines = new FakeRepositories.Fines();
		loans = new FakeRepositories.Loans().withActiveLoans(2);
		parameters = new FakeRepositories.Parameters().with(15, 2);
	}

	@Test
	@DisplayName("busca al usuario con los repositorios que recibió y entrega su situación")
	void findsUserWithItsRepositories() {
		final FoundUserDomain found = useCase().execute(new FindUserDomain(EMAIL));

		assertEquals(USER, found.id());
		assertEquals(2, found.activeLoans());
		assertEquals(2, found.maxLoans());
		assertEquals(List.of(MessageKey.USER_AT_LOAN_LIMIT), found.blocks());
	}

	@Test
	@DisplayName("si no llega la búsqueda pide el correo")
	void requiresEmailWhenSearchIsMissing() {
		final FindUserUseCaseImpl useCase = useCase();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.USER_EMAIL_REQUIRED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no se deja crear sin alguno de sus repositorios")
	void requiresEveryRepository() {
		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> new FindUserUseCaseImpl(users, fines, null, parameters));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

	private FindUserUseCaseImpl useCase() {
		return new FindUserUseCaseImpl(users, fines, loans, parameters);
	}

}
