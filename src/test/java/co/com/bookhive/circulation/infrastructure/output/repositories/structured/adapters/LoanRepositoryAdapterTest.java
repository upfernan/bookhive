package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LoanJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.PageCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class LoanRepositoryAdapterTest {

	private static final UUID LOAN = UUIDHelper.generateNew();
	private static final UUID USER = UUIDHelper.generateNew();

	@Test
	@DisplayName("guarda el préstamo convertido a entidad JPA")
	void createsLoan() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing();
		final LoanEntity loan = new LoanEntity();
		loan.setId(LOAN);

		adapter(repository).create(loan);

		assertEquals(1, repository.saved().size());
		assertEquals(LOAN, repository.saved().get(0).getId());
	}

	@Test
	@DisplayName("busca el préstamo por su identificador")
	void findsLoanById() {
		final LoanRepositoryAdapter adapter = adapter(FakeJpaRepository.storing(jpaLoan()));

		assertEquals(Optional.of(LOAN), adapter.findById(LOAN).map(LoanEntity::getId));
	}

	@Test
	@DisplayName("devuelve vacío si el préstamo no existe")
	void returnsEmptyWhenLoanDoesNotExist() {
		final LoanRepositoryAdapter adapter = adapter(FakeJpaRepository.storing());

		assertTrue(adapter.findById(LOAN).isEmpty());
	}

	@Test
	@DisplayName("consulta la página pedida")
	void findsRequestedPage() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing(jpaLoan());

		final List<LoanEntity> loans = adapter(repository).findAll(PageCriteria.of(2, 10));

		assertEquals(1, loans.size());
		assertEquals(2, repository.pages().get(0).getPageNumber());
		assertEquals(10, repository.pages().get(0).getPageSize());
	}

	@Test
	@DisplayName("sin página usa la primera con 20 elementos")
	void findsDefaultPage() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing(jpaLoan());

		adapter(repository).findAll(null);

		assertEquals(0, repository.pages().get(0).getPageNumber());
		assertEquals(20, repository.pages().get(0).getPageSize());
	}

	@Test
	@DisplayName("filtra los préstamos activos del usuario")
	void findsActiveLoansOfUser() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing(jpaLoan());

		final List<LoanEntity> loans = adapter(repository).findByCriteria(LoanCriteria.activeByUser(USER));

		assertEquals(1, loans.size());
		assertEquals(List.of("user.id=" + USER, "status=" + LoanEntity.STATUS_ACTIVE), repository.filters());
	}

	@Test
	@DisplayName("sin usuario solo filtra por estado")
	void filtersOnlyByStatusWithoutUser() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing();

		adapter(repository).findByCriteria(LoanCriteria.activeByUser(null));

		assertEquals(List.of("status=" + LoanEntity.STATUS_ACTIVE), repository.filters());
	}

	private static LoanRepositoryAdapter adapter(final FakeJpaRepository<LoanJpaEntity> repository) {
		return new LoanRepositoryAdapter(repository.as(LoanJpaRepository.class),
				new FakeEntityMapper<>(LoanRepositoryAdapterTest::toEntity, LoanRepositoryAdapterTest::toJpaEntity));
	}

	private static LoanJpaEntity jpaLoan() {
		final LoanJpaEntity jpaEntity = new LoanJpaEntity();
		jpaEntity.setId(LOAN);
		return jpaEntity;
	}

	private static LoanEntity toEntity(final LoanJpaEntity jpaEntity) {
		final LoanEntity entity = new LoanEntity();
		entity.setId(jpaEntity.getId());
		return entity;
	}

	private static LoanJpaEntity toJpaEntity(final LoanEntity entity) {
		final LoanJpaEntity jpaEntity = new LoanJpaEntity();
		jpaEntity.setId(entity.getId());
		return jpaEntity;
	}

}
