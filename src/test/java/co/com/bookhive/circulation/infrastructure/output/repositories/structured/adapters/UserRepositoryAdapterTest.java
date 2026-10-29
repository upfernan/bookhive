package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.UserJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.UserJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

class UserRepositoryAdapterTest {

	private static final UUID USER = UUIDHelper.generateNew();

	@Test
	@DisplayName("busca al usuario por correo sin distinguir mayúsculas ni espacios")
	void findsUserByEmailIgnoringCase() {
		final FakeJpaRepository<UserJpaEntity> repository = FakeJpaRepository.storing(jpaUser());

		final List<UserEntity> users = adapter(repository)
				.findByCriteria(UserCriteria.byEmail("  Ana.Gomez@Uco.edu.co "));

		assertEquals(1, users.size());
		assertEquals(USER, users.get(0).getId());
		assertEquals(List.of("lower(email)=ana.gomez@uco.edu.co"), repository.filters());
	}

	@Test
	@DisplayName("sin correo no consulta la base, para no traer a todos los usuarios")
	void returnsNothingWithoutEmail() {
		final FakeJpaRepository<UserJpaEntity> repository = FakeJpaRepository.storing(jpaUser());

		assertTrue(adapter(repository).findByCriteria(UserCriteria.byEmail("   ")).isEmpty());
		assertTrue(adapter(repository).findByCriteria(null).isEmpty());
		assertTrue(repository.filters().isEmpty());
	}

	@Test
	@DisplayName("busca lectores no depurados cuyo nombre o correo contenga el texto, ordenados y con tope")
	void findsReadersMatchingText() {
		final FakeJpaRepository<UserJpaEntity> repository = FakeJpaRepository.storing(jpaUser());

		final List<UserEntity> users = adapter(repository).findByCriteria(UserCriteria.matching("Gómez", 10));

		assertEquals(1, users.size());
		assertEquals(List.of(
				"status!=" + UserJpaEntity.STATUS_PURGED,
				"translate(lower(name)) like %gomez% escape \\",
				"lower(email) like %gomez% escape \\"), repository.filters());
		assertEquals(PageRequest.of(0, 10, Sort.by("name", "email")), repository.pages().get(0));
	}

	@Test
	@DisplayName("los comodines del texto se buscan como letras y no como comodines")
	void escapesLikeWildcards() {
		assertEquals("100\\%", UserRepositoryAdapter.escapeLike("100%"));
		assertEquals("ana\\_g", UserRepositoryAdapter.escapeLike("ana_g"));
		assertEquals("a\\\\b", UserRepositoryAdapter.escapeLike("a\\b"));
		assertEquals("ana", UserRepositoryAdapter.escapeLike("ana"));
	}

	@Test
	@DisplayName("sin texto o sin tope la búsqueda de lectores no consulta la base")
	void returnsNothingWithoutText() {
		final FakeJpaRepository<UserJpaEntity> repository = FakeJpaRepository.storing(jpaUser());

		assertTrue(adapter(repository).findByCriteria(UserCriteria.matching("  ", 10)).isEmpty());
		assertTrue(adapter(repository).findByCriteria(UserCriteria.matching("ana", 0)).isEmpty());
		assertTrue(repository.filters().isEmpty());
	}

	@Test
	@DisplayName("busca el usuario por su identificador y sin identificador no consulta")
	void findsUserById() {
		final UserRepositoryAdapter adapter = adapter(FakeJpaRepository.storing(jpaUser()));

		assertEquals(Optional.of(USER), adapter.findById(USER).map(UserEntity::getId));
		assertTrue(adapter.findById(null).isEmpty());
	}

	@Test
	@DisplayName("consulta la página pedida")
	void findsRequestedPage() {
		final FakeJpaRepository<UserJpaEntity> repository = FakeJpaRepository.storing(jpaUser());

		assertEquals(1, adapter(repository).findAll(PageCriteria.of(2, 10)).size());
		assertEquals(2, repository.pages().get(0).getPageNumber());
		assertEquals(1, adapter(repository).findAll(null).size());
	}

	private static UserRepositoryAdapter adapter(final FakeJpaRepository<UserJpaEntity> repository) {
		return new UserRepositoryAdapter(repository.as(UserJpaRepository.class),
				new FakeEntityMapper<>(UserRepositoryAdapterTest::toEntity, UserRepositoryAdapterTest::toJpaEntity));
	}

	private static UserJpaEntity jpaUser() {
		final UserJpaEntity jpaEntity = new UserJpaEntity();
		jpaEntity.setId(USER);
		return jpaEntity;
	}

	private static UserEntity toEntity(final UserJpaEntity jpaEntity) {
		final UserEntity entity = new UserEntity();
		entity.setId(jpaEntity.getId());
		return entity;
	}

	private static UserJpaEntity toJpaEntity(final UserEntity entity) {
		final UserJpaEntity jpaEntity = new UserJpaEntity();
		jpaEntity.setId(entity.getId());
		return jpaEntity;
	}

}
