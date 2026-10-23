package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LoanJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

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
	@DisplayName("busca el préstamo activo o vencido del ejemplar que se devuelve")
	void findsOpenLoanOfBookCopy() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing(jpaLoan());
		final UUID bookCopy = UUID.randomUUID();

		adapter(repository).findByCriteria(LoanCriteria.openForBookCopy(bookCopy));

		assertEquals(List.of("bookCopy.id=" + bookCopy, "status en [ACTIVO, VENCIDO]"), repository.filters());
	}

	@Test
	@DisplayName("actualiza el préstamo al devolverlo")
	void updatesLoan() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing();
		final LoanEntity loan = new LoanEntity();
		loan.setId(LOAN);

		adapter(repository).update(loan);

		assertEquals(LOAN, repository.saved().get(0).getId());
	}

	@Test
	@DisplayName("sin usuario solo filtra por estado")
	void filtersOnlyByStatusWithoutUser() {
		final FakeJpaRepository<LoanJpaEntity> repository = FakeJpaRepository.storing();

		adapter(repository).findByCriteria(LoanCriteria.activeByUser(null));

		assertEquals(List.of("status=" + LoanEntity.STATUS_ACTIVE), repository.filters());
	}

	@Test
	@DisplayName("si dos préstamos del mismo ejemplar llegan al tiempo el segundo se rechaza como ejemplar no disponible")
	void simultaneousLoanIsConflict() {
		final LoanRepositoryAdapter adapter = failingOnSave(new DataIntegrityViolationException(
				"No se pudo guardar.",
				new ConstraintViolationException("Duplicado.", new SQLException("Duplicado.", "23505"),
						"uq_loan_book_copy_active")));
		final LoanEntity loan = new LoanEntity();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.create(loan));

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertEquals(MessageKey.BOOK_COPY_NOT_AVAILABLE, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("el choque también se reconoce cuando solo llega el error del driver")
	void simultaneousLoanFromDriverIsConflict() {
		final LoanRepositoryAdapter adapter = failingOnSave(new DataIntegrityViolationException(
				"No se pudo guardar.",
				new SQLException("duplicate key value violates unique constraint \"uq_loan_book_copy_active\"",
						"23505")));
		final LoanEntity loan = new LoanEntity();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.create(loan));

		assertEquals(MessageKey.BOOK_COPY_NOT_AVAILABLE, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("otra restricción de la base de datos sigue como error inesperado")
	void otherConstraintIsRethrown() {
		final DataIntegrityViolationException failure = new DataIntegrityViolationException(
				"No se pudo guardar.",
				new ConstraintViolationException("Otra.", new SQLException("Otra.", "23505"), "uq_book_copy_barcode"));
		final LoanRepositoryAdapter adapter = failingOnSave(failure);
		final LoanEntity loan = new LoanEntity();

		assertSame(failure, assertThrows(DataIntegrityViolationException.class, () -> adapter.create(loan)));
	}

	@Test
	@DisplayName("un error del driver que no es de duplicado sigue como error inesperado")
	void otherDriverErrorIsRethrown() {
		final IllegalStateException failure =
				new IllegalStateException("Falló.", new SQLException("uq_loan_book_copy_active", "08006"));
		final LoanRepositoryAdapter adapter = failingOnSave(failure);
		final LoanEntity loan = new LoanEntity();

		assertSame(failure, assertThrows(IllegalStateException.class, () -> adapter.create(loan)));
	}

	private static LoanRepositoryAdapter failingOnSave(final RuntimeException failure) {
		final LoanJpaRepository repository = (LoanJpaRepository) Proxy.newProxyInstance(
				LoanJpaRepository.class.getClassLoader(),
				new Class<?>[] { LoanJpaRepository.class },
				(instance, method, arguments) -> {
					throw failure;
				});
		return new LoanRepositoryAdapter(repository, new FakeEntityMapper<>(LoanRepositoryAdapterTest::toEntity, LoanRepositoryAdapterTest::toJpaEntity));
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
