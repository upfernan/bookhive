package co.com.bookhive.circulation.services.usecase.user.searchreaders.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.user.searchreaders.FoundReaderDomain;
import co.com.bookhive.circulation.domain.user.searchreaders.SearchReadersDomain;
import co.com.bookhive.circulation.services.input.adapters.SearchReadersInputAdapter;
import co.com.bookhive.circulation.services.input.dto.request.SearchReadersRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FoundReaderResponseDTO;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class SearchReadersUseCaseImplTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();
	private static final UUID ANA = UUIDHelper.generateNew();

	private final FakeRepositories.Users users = new FakeRepositories.Users()
			.with(FakeRepositories.user(ANA, "Ana Gómez", "ana.gomez@uco.edu.co"));
	private final List<String> audited = new ArrayList<>();
	private final SessionContextPort session = new FakeSession();
	private final AuditPort audit = this::audit;

	@Test
	@DisplayName("devuelve los lectores encontrados y deja en la auditoría cuántos fueron, sin el texto buscado")
	void findsReadersAndAuditsOnlyTheCount() {
		final List<FoundReaderDomain> found = useCase().execute(new SearchReadersDomain("gómez"));

		assertEquals(List.of(new FoundReaderDomain(ANA, "Ana Gómez", "ana.gomez@uco.edu.co")), found);
		assertEquals(List.of(AuditPort.READERS_SEARCHED + " " + INSTITUTION + " " + LIBRARIAN + " 1 resultado(s)"), audited);
		assertFalse(audited.get(0).contains("gómez"));
	}

	@Test
	@DisplayName("con un texto inválido no busca ni deja auditoría")
	void rejectsInvalidTextWithoutAuditing() {
		final SearchReadersUseCaseImpl useCase = useCase();
		final SearchReadersDomain search = new SearchReadersDomain("an");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(search));

		assertEquals(MessageKey.USER_SEARCH_TEXT_INVALID, exception.getUserMessageKey());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("una petición nula se trata como búsqueda vacía y se rechaza")
	void rejectsNullDomain() {
		final SearchReadersUseCaseImpl useCase = useCase();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.USER_SEARCH_TEXT_INVALID, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no deja crear el caso de uso si falta una dependencia")
	void requiresDependencies() {
		assertEquals(BookHiveException.Kind.UNEXPECTED, assertThrows(BookHiveException.class,
				() -> new SearchReadersUseCaseImpl(null, session, audit)).getKind());
		assertThrows(BookHiveException.class, () -> new SearchReadersUseCaseImpl(users, null, audit));
		assertThrows(BookHiveException.class, () -> new SearchReadersUseCaseImpl(users, session, null));
	}

	@Test
	@DisplayName("el puerto de entrada devuelve nombre y correo de cada lector")
	void inputAdapterReturnsReaders() {
		final SearchReadersInputAdapter adapter = new SearchReadersInputAdapter(useCase());

		assertEquals(List.of(new FoundReaderResponseDTO(ANA, "Ana Gómez", "ana.gomez@uco.edu.co")),
				adapter.execute(new SearchReadersRequestDTO("ana")));
		assertEquals(List.of(), new SearchReadersInputAdapter(domain -> null).execute(null));
	}

	private SearchReadersUseCaseImpl useCase() {
		return new SearchReadersUseCaseImpl(users, session, audit);
	}

	private void audit(final String action, final UUID institution, final UUID person, final String detail) {
		audited.add(action + " " + institution + " " + person + " " + detail);
	}

	private static final class FakeSession implements SessionContextPort {

		@Override
		public UUID getInstitution() {
			return INSTITUTION;
		}

		@Override
		public UUID getLibrarian() {
			return LIBRARIAN;
		}

	}

}
