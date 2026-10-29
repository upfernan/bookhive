package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.BookCopyJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

class BookCopyRepositoryAdapterTest {

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();

	@Test
	@DisplayName("busca el ejemplar por su identificador")
	void findsBookCopyById() {
		final BookCopyRepositoryAdapter adapter = adapter(FakeJpaRepository.storing(jpaBookCopy()));

		assertEquals(Optional.of(BOOK_COPY), adapter.findById(BOOK_COPY).map(BookCopyEntity::getId));
	}

	@Test
	@DisplayName("devuelve vacío si el ejemplar no existe")
	void returnsEmptyWhenBookCopyDoesNotExist() {
		final BookCopyRepositoryAdapter adapter = adapter(FakeJpaRepository.storing());

		assertTrue(adapter.findById(BOOK_COPY).isEmpty());
	}

	@Test
	@DisplayName("consulta la página pedida")
	void findsRequestedPage() {
		final FakeJpaRepository<BookCopyJpaEntity> repository = FakeJpaRepository.storing(jpaBookCopy());

		final List<BookCopyEntity> bookCopies = adapter(repository).findAll(PageCriteria.of(1, 5));

		assertEquals(1, bookCopies.size());
		assertEquals(1, repository.pages().get(0).getPageNumber());
		assertEquals(5, repository.pages().get(0).getPageSize());
	}

	@Test
	@DisplayName("busca los ejemplares de varios libros dejando por fuera los dados de baja")
	void findsCopiesOfBooksExceptWithdrawn() {
		final FakeJpaRepository<BookCopyJpaEntity> repository = FakeJpaRepository.storing(jpaBookCopy());
		final UUID book = UUIDHelper.generateNew();

		final List<BookCopyEntity> bookCopies =
				adapter(repository).findByCriteria(BookCopyCriteria.inService(List.of(book)));

		assertEquals(1, bookCopies.size());
		assertEquals(List.of("book en [" + book + "]", "status!=" + BookCopyEntity.STATUS_WITHDRAWN), repository.filters());
	}

	@Test
	@DisplayName("sin libros no consulta ejemplares")
	void skipsSearchWithoutBooks() {
		final FakeJpaRepository<BookCopyJpaEntity> repository = FakeJpaRepository.storing(jpaBookCopy());
		final BookCopyRepositoryAdapter adapter = adapter(repository);

		assertTrue(adapter.findByCriteria(null).isEmpty());
		assertTrue(adapter.findByCriteria(BookCopyCriteria.inService(List.of())).isEmpty());
		assertTrue(repository.filters().isEmpty());
	}

	@Test
	@DisplayName("busca el ejemplar por su código de barras, sin filtrar por libro ni por estado")
	void findsBookCopyByBarcode() {
		final FakeJpaRepository<BookCopyJpaEntity> repository = FakeJpaRepository.storing(jpaBookCopy());

		final List<BookCopyEntity> bookCopies =
				adapter(repository).findByCriteria(BookCopyCriteria.byBarcode("UCO-0001"));

		assertEquals(1, bookCopies.size());
		assertEquals(List.of("barcode=UCO-0001"), repository.filters());
	}

	@Test
	@DisplayName("con un código vacío no consulta ejemplares")
	void skipsSearchWithoutBarcode() {
		final FakeJpaRepository<BookCopyJpaEntity> repository = FakeJpaRepository.storing(jpaBookCopy());

		assertTrue(adapter(repository).findByCriteria(BookCopyCriteria.byBarcode("  ")).isEmpty());
		assertTrue(repository.filters().isEmpty());
	}

	@Test
	@DisplayName("guarda el ejemplar actualizado")
	void updatesBookCopy() {
		final FakeJpaRepository<BookCopyJpaEntity> repository = FakeJpaRepository.storing();
		final BookCopyEntity bookCopy = new BookCopyEntity();
		bookCopy.setId(BOOK_COPY);

		adapter(repository).update(bookCopy);

		assertEquals(BOOK_COPY, repository.saved().get(0).getId());
	}

	@Test
	@DisplayName("cambia el estado solo si el ejemplar sigue en el estado esperado, en una sola instrucción")
	void changesStatusOnlyFromTheExpectedOne() {
		final FakeJpaRepository<BookCopyJpaEntity> changed = FakeJpaRepository.storing();
		final FakeJpaRepository<BookCopyJpaEntity> unchanged = FakeJpaRepository.<BookCopyJpaEntity>storing().changingRows(0);

		assertTrue(adapter(changed).changeStatus(BOOK_COPY, " DISPONIBLE ", "PRESTADO"));
		assertEquals(List.of(BOOK_COPY + ":DISPONIBLE->PRESTADO"), changed.filters());
		assertFalse(adapter(unchanged).changeStatus(BOOK_COPY, "DISPONIBLE", "PRESTADO"));
	}

	@Test
	@DisplayName("sin ejemplar o sin estados no toca la base")
	void skipsIncompleteStatusChange() {
		final FakeJpaRepository<BookCopyJpaEntity> repository = FakeJpaRepository.storing();
		final BookCopyRepositoryAdapter adapter = adapter(repository);

		assertFalse(adapter.changeStatus(null, "DISPONIBLE", "PRESTADO"));
		assertFalse(adapter.changeStatus(BOOK_COPY, " ", "PRESTADO"));
		assertFalse(adapter.changeStatus(BOOK_COPY, "DISPONIBLE", null));
		assertTrue(repository.filters().isEmpty());
	}

	private static BookCopyRepositoryAdapter adapter(final FakeJpaRepository<BookCopyJpaEntity> repository) {
		return new BookCopyRepositoryAdapter(repository.as(BookCopyJpaRepository.class),
				new FakeEntityMapper<>(BookCopyRepositoryAdapterTest::toEntity,
						BookCopyRepositoryAdapterTest::toJpaEntity));
	}

	private static BookCopyJpaEntity jpaBookCopy() {
		final BookCopyJpaEntity jpaEntity = new BookCopyJpaEntity();
		jpaEntity.setId(BOOK_COPY);
		return jpaEntity;
	}

	private static BookCopyEntity toEntity(final BookCopyJpaEntity jpaEntity) {
		final BookCopyEntity entity = new BookCopyEntity();
		entity.setId(jpaEntity.getId());
		return entity;
	}

	private static BookCopyJpaEntity toJpaEntity(final BookCopyEntity entity) {
		final BookCopyJpaEntity jpaEntity = new BookCopyJpaEntity();
		jpaEntity.setId(entity.getId());
		return jpaEntity;
	}

}
