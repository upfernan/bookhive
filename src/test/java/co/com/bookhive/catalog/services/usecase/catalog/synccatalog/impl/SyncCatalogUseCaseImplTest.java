package co.com.bookhive.catalog.services.usecase.catalog.synccatalog.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.domain.catalog.synccatalog.SyncCatalogDomain;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.testdata.FakeCatalog;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class SyncCatalogUseCaseImplTest {

	@Test
	@DisplayName("si el buscador no tiene los mismos libros activos que la base, los recarga todos por lotes")
	void reloadsEverythingWhenCountsDiffer() {
		final FakeCatalog.Books books = new FakeCatalog.Books();
		for (int number = 0; number < SyncCatalogUseCaseImpl.BATCH_SIZE + 5; number++) {
			books.with(FakeCatalog.book("Libro " + number, BookEntity.STATUS_ACTIVE));
		}
		books.with(FakeCatalog.book("Retirado", "BAJA"));
		final FakeCatalog.Index index = new FakeCatalog.Index().holding(3);
		final SyncCatalogUseCaseImpl useCase = new SyncCatalogUseCaseImpl(books, new FakeCatalog.Changes(), index);

		final SyncCatalogDomain result = useCase.execute();

		assertTrue(result.fullReload());
		assertEquals(SyncCatalogUseCaseImpl.BATCH_SIZE + 5, result.indexed());
		assertEquals(SyncCatalogUseCaseImpl.BATCH_SIZE + 5, index.saved().size());
		assertTrue(index.saved().stream().allMatch(BookEntity::isActive));
	}

	@Test
	@DisplayName("una recarga exacta de un lote no pide una página más")
	void stopsAfterLastFullPage() {
		final FakeCatalog.Books books = new FakeCatalog.Books();
		for (int number = 0; number < SyncCatalogUseCaseImpl.BATCH_SIZE; number++) {
			books.with(FakeCatalog.book("Libro " + number, BookEntity.STATUS_ACTIVE));
		}
		final FakeCatalog.Index index = new FakeCatalog.Index();

		new SyncCatalogUseCaseImpl(books, new FakeCatalog.Changes(), index).execute();

		assertEquals(SyncCatalogUseCaseImpl.BATCH_SIZE, index.saved().size());
		assertEquals(2, books.searches().size());
	}

	@Test
	@DisplayName("envía los cambios pendientes: guarda los activos, retira los dados de baja o borrados y los marca como enviados")
	void sendsPendingChanges() {
		final BookEntity active = FakeCatalog.book("Activo", BookEntity.STATUS_ACTIVE);
		final BookEntity withdrawn = FakeCatalog.book("Retirado", "BAJA");
		final UUID deleted = UUID.randomUUID();
		final FakeCatalog.Books books = new FakeCatalog.Books().with(active).with(withdrawn);
		final FakeCatalog.Changes changes = new FakeCatalog.Changes()
				.with(active.id()).with(active.id()).with(withdrawn.id()).with(deleted);
		final FakeCatalog.Index index = new FakeCatalog.Index().holding(1);

		final SyncCatalogDomain result = new SyncCatalogUseCaseImpl(books, changes, index).execute();

		assertFalse(result.fullReload());
		assertEquals(1, result.indexed());
		assertEquals(2, result.removed());
		assertEquals(List.of(active), index.saved());
		assertEquals(List.of(withdrawn.id(), deleted), index.deleted());
		assertEquals(4, changes.sent().size());
	}

	@Test
	@DisplayName("sin cambios pendientes y con el buscador al día no envía nada")
	void doesNothingWhenUpToDate() {
		final FakeCatalog.Books books = new FakeCatalog.Books().with(FakeCatalog.book("Activo", BookEntity.STATUS_ACTIVE));
		final FakeCatalog.Index index = new FakeCatalog.Index().holding(1);

		final SyncCatalogDomain result = new SyncCatalogUseCaseImpl(books, new FakeCatalog.Changes(), index).execute();

		assertFalse(result.fullReload());
		assertEquals(0, result.indexed());
		assertEquals(0, result.removed());
		assertTrue(index.saved().isEmpty());
		assertTrue(index.deleted().isEmpty());
	}

	@Test
	@DisplayName("si solo hay cambios de libros borrados no intenta guardar nada")
	void onlyDeletesWhenNothingIsActive() {
		final UUID deleted = UUID.randomUUID();
		final FakeCatalog.Changes changes = new FakeCatalog.Changes().with(deleted);
		final FakeCatalog.Index index = new FakeCatalog.Index();

		new SyncCatalogUseCaseImpl(new FakeCatalog.Books(), changes, index).execute();

		assertTrue(index.saved().isEmpty());
		assertEquals(List.of(deleted), index.deleted());
	}

	@Test
	@DisplayName("no se puede crear sin sus repositorios")
	void requiresDependencies() {
		final FakeCatalog.Books books = new FakeCatalog.Books();
		final FakeCatalog.Changes changes = new FakeCatalog.Changes();
		final FakeCatalog.Index index = new FakeCatalog.Index();

		assertThrows(BookHiveException.class, () -> new SyncCatalogUseCaseImpl(null, changes, index));
		assertThrows(BookHiveException.class, () -> new SyncCatalogUseCaseImpl(books, null, index));
		assertThrows(BookHiveException.class, () -> new SyncCatalogUseCaseImpl(books, changes, null));
	}

}
