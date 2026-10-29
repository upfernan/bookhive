package co.com.bookhive.circulation.domain.reservation.reservebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class ReserveBookDomainTest {

	@Test
	@DisplayName("convierte los nulos en valores por defecto y toma el usuario de la sesión")
	void keepsBookAndSessionUser() {
		final UUID book = UUID.randomUUID();
		final UUID user = UUID.randomUUID();

		final ReserveBookDomain domain = new ReserveBookDomain(book, null);
		final ReserveBookDomain withUser = domain.withUser(user);

		assertEquals(UUIDHelper.getDefaultValue(), domain.getUser());
		assertEquals(book, withUser.getBook());
		assertEquals(user, withUser.getUser());
		assertEquals(UUIDHelper.getDefaultValue(), new ReserveBookDomain(null, null).getBook());
	}

	@Test
	@DisplayName("validar sin repositorios es un error del programa, no del usuario")
	void validateWithoutRepositoriesIsUnexpected() {
		final ReserveBookDomain domain = new ReserveBookDomain(UUID.randomUUID(), UUID.randomUUID());

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, domain::validate).getKind());
	}

}
