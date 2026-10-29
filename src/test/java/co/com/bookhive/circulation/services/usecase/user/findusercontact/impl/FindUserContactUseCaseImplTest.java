package co.com.bookhive.circulation.services.usecase.user.findusercontact.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.user.findusercontact.FindUserContactDomain;
import co.com.bookhive.circulation.services.input.adapters.FindUserContactInputAdapter;
import co.com.bookhive.circulation.services.input.dto.request.FindUserContactRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserContactResponseDTO;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;

class FindUserContactUseCaseImplTest {

	private final UUID active = UUID.randomUUID();
	private final UUID suspended = UUID.randomUUID();
	private final FakeRepositories.Users users = new FakeRepositories.Users()
			.with(user(active, UserEntity.STATUS_ACTIVE))
			.with(user(suspended, UserEntity.STATUS_SUSPENDED));

	@Test
	@DisplayName("entrega el nombre y el correo solo de usuarios activos")
	void findsOnlyActiveUsers() {
		final FindUserContactUseCaseImpl useCase = new FindUserContactUseCaseImpl(users);

		assertEquals(new FindUserContactDomain(active, "Ana Pérez", "ana@uco.edu.co"),
				useCase.execute(new FindUserContactDomain(active, "", "")));
		assertEquals(new FindUserContactDomain(suspended, "", ""),
				useCase.execute(new FindUserContactDomain(suspended, "", "")));
		assertEquals(new FindUserContactDomain(null, "", ""), useCase.execute(null));
	}

	@Test
	@DisplayName("el puerto de entrada devuelve el contacto del usuario")
	void inputAdapterReturnsContact() {
		final FindUserContactInputAdapter adapter =
				new FindUserContactInputAdapter(new FindUserContactUseCaseImpl(users));

		assertEquals(new UserContactResponseDTO(active, "Ana Pérez", "ana@uco.edu.co"),
				adapter.execute(new FindUserContactRequestDTO(active)));
		assertEquals(new UserContactResponseDTO(null, "", ""), adapter.execute(null));
	}

	private static UserEntity user(final UUID id, final String status) {
		final UserEntity user = FakeRepositories.user(id, "Ana Pérez", "ana@uco.edu.co");
		user.setStatus(status);
		return user;
	}

}
