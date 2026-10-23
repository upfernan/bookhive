package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LoanReturnJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanReturnJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanReturnCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LoanReturnRepositoryAdapterTest {

	private static final UUID LOAN = UUID.randomUUID();

	@Test
	@DisplayName("guarda la devolución del préstamo")
	void createsReturn() {
		final FakeJpaRepository<LoanReturnJpaEntity> repository = FakeJpaRepository.storing();
		final LoanReturnEntity loanReturn = new LoanReturnEntity();
		loanReturn.setLoan(LOAN);

		adapter(repository.as(LoanReturnJpaRepository.class)).create(loanReturn);

		assertEquals(LOAN, repository.saved().get(0).getLoan());
	}

	@Test
	@DisplayName("si dos devoluciones del mismo préstamo llegan al tiempo la segunda se rechaza")
	void secondReturnIsConflict() {
		final LoanReturnRepositoryAdapter adapter = failingOnSave(new DataIntegrityViolationException("Duplicado.",
				new ConstraintViolationException("Duplicado.", new SQLException("Duplicado.", "23505"),
						LoanReturnRepositoryAdapter.ONE_RETURN_PER_LOAN)));
		final LoanReturnRepositoryAdapter fromDriver = failingOnSave(new DataIntegrityViolationException("Duplicado.",
				new SQLException("duplicate key value violates unique constraint \"uq_loan_return_loan\"", "23505")));
		final LoanReturnEntity loanReturn = new LoanReturnEntity();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.create(loanReturn));

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN, exception.getUserMessageKey());
		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN,
				assertThrows(BookHiveException.class, () -> fromDriver.create(loanReturn)).getUserMessageKey());
	}

	@Test
	@DisplayName("otros errores de la base de datos siguen como inesperados")
	void otherErrorsAreRethrown() {
		final DataIntegrityViolationException otherConstraint = new DataIntegrityViolationException("Otra.",
				new ConstraintViolationException("Otra.", new SQLException("Otra.", "23503"), "fk_loan_return_loan"));
		final IllegalStateException driverFailure =
				new IllegalStateException("Falló.", new SQLException("uq_loan_return_loan", "08006"));
		final LoanReturnEntity loanReturn = new LoanReturnEntity();
		final LoanReturnRepositoryAdapter first = failingOnSave(otherConstraint);
		final LoanReturnRepositoryAdapter second = failingOnSave(driverFailure);

		assertSame(otherConstraint, assertThrows(DataIntegrityViolationException.class, () -> first.create(loanReturn)));
		assertSame(driverFailure, assertThrows(IllegalStateException.class, () -> second.create(loanReturn)));
	}

	@Test
	@DisplayName("busca la devolución de hoy de un ejemplar entre los préstamos de ese ejemplar")
	void findsReturnOfBookCopyOnDate() {
		final LoanReturnJpaEntity stored = new LoanReturnJpaEntity();
		stored.setLoan(LOAN);
		final FakeJpaRepository<LoanReturnJpaEntity> repository = FakeJpaRepository.storing(stored);
		final UUID bookCopy = UUID.randomUUID();
		final LocalDate today = LocalDate.of(2026, 10, 23);

		final List<LoanReturnEntity> found = adapter(repository.as(LoanReturnJpaRepository.class))
				.findByCriteria(LoanReturnCriteria.ofBookCopyOn(bookCopy, today));

		assertEquals(LOAN, found.get(0).getLoan());
		assertEquals(3, repository.filters().size());
		assertEquals("bookCopy.id=" + bookCopy, repository.filters().get(0));
		assertTrue(repository.filters().get(1).startsWith("loan en "));
		assertEquals("returnDate=" + today, repository.filters().get(2));
	}

	@Test
	@DisplayName("sin ejemplar o sin fecha no consulta")
	void skipsIncompleteSearch() {
		final FakeJpaRepository<LoanReturnJpaEntity> repository = FakeJpaRepository.storing(new LoanReturnJpaEntity());
		final LoanReturnRepositoryAdapter adapter = adapter(repository.as(LoanReturnJpaRepository.class));

		assertTrue(adapter.findByCriteria(LoanReturnCriteria.ofBookCopyOn(null, LocalDate.of(2026, 10, 23))).isEmpty());
		assertTrue(adapter.findByCriteria(LoanReturnCriteria.ofBookCopyOn(UUID.randomUUID(), null)).isEmpty());
		assertTrue(adapter.findByCriteria(null).isEmpty());
		assertTrue(repository.filters().isEmpty());
	}

	private static LoanReturnRepositoryAdapter failingOnSave(final RuntimeException failure) {
		final LoanReturnJpaRepository repository = (LoanReturnJpaRepository) Proxy.newProxyInstance(
				LoanReturnJpaRepository.class.getClassLoader(),
				new Class<?>[] { LoanReturnJpaRepository.class },
				(instance, method, arguments) -> {
					throw failure;
				});
		return adapter(repository);
	}

	private static LoanReturnRepositoryAdapter adapter(final LoanReturnJpaRepository repository) {
		return new LoanReturnRepositoryAdapter(repository, new FakeEntityMapper<>(jpa -> {
			final LoanReturnEntity entity = new LoanReturnEntity();
			entity.setLoan(jpa.getLoan());
			return entity;
		}, entity -> {
			final LoanReturnJpaEntity jpa = new LoanReturnJpaEntity();
			jpa.setLoan(entity.getLoan());
			jpa.setReturnDate(entity.getReturnDate());
			jpa.setOverdueDays(entity.getOverdueDays());
			jpa.setLibrarian(entity.getLibrarian());
			return jpa;
		}));
	}

}
