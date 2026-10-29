package co.com.bookhive.circulation.domain.user.searchreaders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SearchReadersDomainTest {

	private static final UUID ANA = UUIDHelper.generateNew();

	private FakeRepositories.Users users;

	@BeforeEach
	void prepare() {
		users = new FakeRepositories.Users()
				.with(FakeRepositories.user(ANA, "Ana Gómez", "ana.gomez@uco.edu.co"))
				.with(FakeRepositories.user(UUIDHelper.generateNew(), "Pedro Ruiz", "pedro.ruiz@uco.edu.co"));
	}

	@Test
	@DisplayName("encuentra al lector por parte del nombre sin distinguir mayúsculas")
	void findsByName() {
		assertEquals(List.of(new FoundReaderDomain(ANA, "Ana Gómez", "ana.gomez@uco.edu.co")),
				search("  GÓMEZ ").find());
	}

	@Test
	@DisplayName("encuentra al lector aunque se escriba sin tildes")
	void findsWithoutAccents() {
		assertEquals(ANA, search("gomez").find().get(0).id());
	}

	@Test
	@DisplayName("encuentra al lector por parte del correo")
	void findsByEmail() {
		assertEquals(ANA, search("ana.go").find().get(0).id());
	}

	@Test
	@DisplayName("muestra lectores suspendidos y dados de baja: pueden tener libros por devolver")
	void findsSuspendedAndRetiredReaders() {
		final UUID suspendedId = UUIDHelper.generateNew();
		final UUID retiredId = UUIDHelper.generateNew();
		final UserEntity suspended = FakeRepositories.user(suspendedId, "Ana Suspendida", "ana.s@uco.edu.co");
		suspended.setStatus(UserEntity.STATUS_SUSPENDED);
		final UserEntity retired = FakeRepositories.user(retiredId, "Ana Retirada", "ana.r@uco.edu.co");
		retired.setStatus(UserEntity.STATUS_RETIRED);
		users.with(suspended).with(retired);

		assertEquals(List.of(ANA, suspendedId, retiredId),
				search("ana").find().stream().map(FoundReaderDomain::id).toList());
	}

	@Test
	@DisplayName("no muestra lectores depurados: ya no tienen datos personales")
	void skipsPurgedReaders() {
		final UserEntity purged = FakeRepositories.user(UUIDHelper.generateNew(), "Ana Depurada", "ana.d@uco.edu.co");
		purged.setStatus(UserLifecycleDomains.PURGED);
		users.with(purged);

		assertEquals(List.of(ANA), search("ana").find().stream().map(FoundReaderDomain::id).toList());
	}

	@Test
	@DisplayName("devuelve como máximo diez lectores aunque coincidan más")
	void limitsResults() {
		for (int i = 0; i < SearchReadersDomain.LIMIT + 5; i++) {
			users.with(FakeRepositories.user(UUIDHelper.generateNew(), "Lector " + i, "lector" + i + "@uco.edu.co"));
		}

		assertEquals(SearchReadersDomain.LIMIT, search("lector").find().size());
	}

	@Test
	@DisplayName("con menos de tres letras pide escribir más")
	void rejectsShortText() {
		final SearchReadersDomain search = search(" an ");

		final BookHiveException exception = assertThrows(BookHiveException.class, search::validate);

		assertEquals(MessageKey.USER_SEARCH_TEXT_INVALID, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
	}

	@Test
	@DisplayName("sin texto o con un texto demasiado largo tampoco busca")
	void rejectsEmptyAndLongText() {
		final SearchReadersDomain empty = search(null);
		final SearchReadersDomain tooLong = search("a".repeat(SearchReadersDomain.MAX_LENGTH + 1));

		assertEquals(MessageKey.USER_SEARCH_TEXT_INVALID,
				assertThrows(BookHiveException.class, empty::validate).getUserMessageKey());
		assertEquals(MessageKey.USER_SEARCH_TEXT_INVALID,
				assertThrows(BookHiveException.class, tooLong::validate).getUserMessageKey());
	}

	@Test
	@DisplayName("con tres letras o con el máximo permitido la validación pasa")
	void acceptsTextWithinLimits() {
		assertDoesNotThrow(search("ana")::validate);
		assertDoesNotThrow(search("a".repeat(SearchReadersDomain.MAX_LENGTH))::validate);
		assertEquals("ana", search("  ana  ").getText());
	}

	@Test
	@DisplayName("sin repositorio no valida ni busca")
	void requiresRepository() {
		final SearchReadersDomain search = new SearchReadersDomain("ana");

		assertEquals(BookHiveException.Kind.UNEXPECTED, assertThrows(BookHiveException.class, search::validate).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED, assertThrows(BookHiveException.class, search::find).getKind());
	}

	@Test
	@DisplayName("los lectores encontrados con nulos quedan con valores por defecto")
	void foundReaderReplacesNulls() {
		final FoundReaderDomain empty = new FoundReaderDomain(null, null, null);

		assertEquals(UUIDHelper.getDefaultValue(), empty.id());
		assertEquals("", empty.name());
		assertEquals("", empty.email());
	}

	private SearchReadersDomain search(final String text) {
		return new SearchReadersDomain(text).withRepositories(users);
	}

}
