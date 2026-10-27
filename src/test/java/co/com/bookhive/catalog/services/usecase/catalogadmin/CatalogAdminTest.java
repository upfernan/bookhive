package co.com.bookhive.catalog.services.usecase.catalogadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.com.bookhive.access.services.input.dto.response.CurrentSessionResponseDTO;
import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.infrastructure.input.controllers.CatalogAdminController;
import co.com.bookhive.catalog.infrastructure.output.externalservices.access.AccessCatalogSessionAdapter;
import co.com.bookhive.catalog.infrastructure.output.externalservices.circulation.CirculationBookCopyInventoryAdapter;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.adapters.CatalogAdminRepositoryAdapter;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.BookJpaRepository;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.CatalogAdminJpaRepository;
import co.com.bookhive.catalog.services.input.dto.request.CategoryRequestDTO;
import co.com.bookhive.catalog.services.input.dto.request.TitleRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.CatalogAdminResponseDTOs;
import co.com.bookhive.catalog.services.input.adapters.CatalogAdminInputAdapters;
import co.com.bookhive.catalog.services.output.ports.BookCopyInventoryPort;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;
import co.com.bookhive.catalog.services.usecase.catalogadmin.changecategorystatus.impl.ChangeCategoryStatusUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.listcategories.impl.ListCategoriesUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.registertitle.impl.RegisterTitleUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.savecategory.impl.SaveCategoryUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.updatetitle.impl.UpdateTitleUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.withdrawtitle.impl.WithdrawTitleUseCaseImpl;
import co.com.bookhive.circulation.services.input.dto.request.RegisterBookCopiesRequestDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class CatalogAdminTest {

	private static final UUID INSTITUTION = UUID.randomUUID();
	private static final UUID LIBRARIAN = UUID.randomUUID();
	private static final UUID BRANCH = UUID.randomUUID();

	private Jpa jpa;
	private List<RegisterBookCopiesRequestDTO> registered;
	private List<UUID> retired;
	private List<String> audited;
	private CatalogAdminController controller;
	private UUID poetry;
	private UUID history;

	@BeforeEach
	void prepare() {
		jpa = new Jpa();
		registered = new ArrayList<>();
		retired = new ArrayList<>();
		audited = new ArrayList<>();
		final CatalogAdminRepositoryAdapter repository = new CatalogAdminRepositoryAdapter(jpa);
		final BookCopyInventoryPort inventory = new CirculationBookCopyInventoryAdapter(request -> {
			registered.add(request);
			return request.barcodes().size();
		}, book -> {
			retired.add(book);
			return 2;
		});
		final AuditPort audit = (action, institution, person, detail) -> audited.add(action + " " + institution + " " + person);
		final CatalogSessionPort session = new AccessCatalogSessionAdapter(() -> new CurrentSessionResponseDTO(INSTITUTION, LIBRARIAN));
		controller = new CatalogAdminController(new CatalogAdminController.Ports(
				CatalogAdminInputAdapters.listCategories(new ListCategoriesUseCaseImpl(repository)),
				CatalogAdminInputAdapters.saveCategory(new SaveCategoryUseCaseImpl(repository, session, audit)),
				CatalogAdminInputAdapters.changeCategoryStatus(new ChangeCategoryStatusUseCaseImpl(repository, session, audit)),
				CatalogAdminInputAdapters.findTitle(new FindTitleUseCaseImpl(repository)),
				CatalogAdminInputAdapters.registerTitle(new RegisterTitleUseCaseImpl(repository, repository, inventory, session, audit)),
				CatalogAdminInputAdapters.updateTitle(new UpdateTitleUseCaseImpl(repository, repository, inventory, session, audit)),
				CatalogAdminInputAdapters.withdrawTitle(new WithdrawTitleUseCaseImpl(repository, inventory, session, audit))));
		poetry = controller.createCategory(new CategoryRequestDTO(null, "Poesía")).getBody().id();
		history = controller.createCategory(new CategoryRequestDTO(null, "Historia")).getBody().id();
		audited.clear();
	}

	@Test
	@DisplayName("crea, lista, renombra y no repite categorías sin importar mayúsculas")
	void managesCategories() {
		final List<CatalogAdminResponseDTOs.Category> listed = controller.categories().getBody();
		final CatalogAdminResponseDTOs.Category renamed =
				controller.renameCategory(poetry, new CategoryRequestDTO(null, "POESÍA")).getBody();

		assertEquals(List.of("Historia", "Poesía"), listed.stream().map(CatalogAdminResponseDTOs.Category::name).toList());
		assertEquals("ACTIVA", listed.get(0).status());
		assertEquals("POESÍA", renamed.name());
		assertEquals(MessageKey.CATEGORY_NAME_TAKEN, keyOf(() -> controller.createCategory(new CategoryRequestDTO(null, " historia "))));
		assertEquals(MessageKey.CATEGORY_NAME_TAKEN, keyOf(() -> controller.renameCategory(poetry, new CategoryRequestDTO(null, "Historia"))));
		assertEquals(MessageKey.CATEGORY_NOT_FOUND, keyOf(() -> controller.renameCategory(UUID.randomUUID(), new CategoryRequestDTO(null, "Otra"))));
		assertEquals(MessageKey.CATEGORY_NAME_INVALID, keyOf(() -> controller.createCategory(null)));
		assertEquals(MessageKey.CATEGORY_NAME_INVALID, keyOf(() -> controller.createCategory(new CategoryRequestDTO(null, "x".repeat(101)))));
		assertEquals(List.of(AuditPort.CATEGORY_SAVED + " " + INSTITUTION + " " + LIBRARIAN), audited);
	}

	@Test
	@DisplayName("una categoría con títulos activos no se desactiva y una inactiva no recibe títulos nuevos")
	void categoryStatusRules() {
		controller.registerTitle(title(null, "Odas", "FISICO", poetry, List.of()));

		assertEquals(MessageKey.CATEGORY_IN_USE, keyOf(() -> controller.deactivateCategory(poetry)));
		assertEquals("INACTIVA", controller.deactivateCategory(history).getBody().status());
		assertEquals(MessageKey.CATEGORY_INACTIVE, keyOf(() -> controller.registerTitle(title(null, "Crónicas", "FISICO", history, List.of()))));
		assertEquals("ACTIVA", controller.activateCategory(history).getBody().status());
		assertEquals(1L, controller.categories().getBody().stream().filter(c -> c.id().equals(poetry)).findFirst().orElseThrow().activeTitles());
		assertEquals(MessageKey.CATEGORY_NOT_FOUND, keyOf(() -> controller.activateCategory(UUID.randomUUID())));
	}

	@Test
	@DisplayName("registra un título con autores, editorial y ejemplares, y lo consulta")
	void registersTitleWithCopies() {
		final var response = controller.registerTitle(new TitleRequestDTO(UUID.randomUUID(), " Cien sonetos ", "978-1-23456-789-7",
				2020, "fisico", poetry, "Editorial Nueva", List.of("Autora Uno", " ", "Autor Dos"), BRANCH, List.of("PO-1", "PO-2")));
		final CatalogAdminResponseDTOs.Title created = response.getBody();
		final CatalogAdminResponseDTOs.Title found = controller.title(created.id()).getBody();

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Cien sonetos", created.title());
		assertEquals("9781234567897", created.isbn());
		assertEquals("FISICO", created.format());
		assertEquals("ACTIVO", created.status());
		assertEquals(List.of("Autor Dos", "Autora Uno"), found.authors());
		assertEquals("Editorial Nueva", found.publisher());
		assertEquals(2020, found.year());
		assertEquals(List.of(new RegisterBookCopiesRequestDTO(created.id(), BRANCH, List.of("PO-1", "PO-2"))), registered);
		assertEquals(List.of(AuditPort.TITLE_REGISTERED + " " + INSTITUTION + " " + LIBRARIAN), audited);
		assertEquals(1, jpa.publishers.size());
	}

	@Test
	@DisplayName("un título digital sin ISBN ni año no pide ejemplares y reutiliza editorial y autores")
	void registersDigitalTitle() {
		controller.registerTitle(new TitleRequestDTO(null, "Uno", null, null, "DIGITAL", poetry, "Nueva", List.of("Ana"), null, null));
		final CatalogAdminResponseDTOs.Title second = controller.registerTitle(
				new TitleRequestDTO(null, "Dos", "", 0, "DIGITAL", poetry, "nueva", List.of("ANA"), null, List.of())).getBody();

		assertTrue(registered.isEmpty());
		assertEquals(0, second.year());
		assertEquals("", second.isbn());
		assertEquals(1, jpa.publishers.size());
		assertEquals(1, jpa.authors.size());
	}

	@Test
	@DisplayName("rechaza títulos con datos inválidos, sin categoría o con ejemplares inválidos")
	void rejectsInvalidTitles() {
		final List<TitleRequestDTO> invalidData = List.of(
				title(null, "", "FISICO", poetry, List.of()),
				title(null, "x".repeat(301), "FISICO", poetry, List.of()),
				title(null, "Odas", "AUDIO", poetry, List.of()),
				new TitleRequestDTO(null, "Odas", null, 2020, "FISICO", poetry, "", List.of("Ana"), null, null),
				new TitleRequestDTO(null, "Odas", null, 2020, "FISICO", poetry, "p".repeat(151), List.of("Ana"), null, null),
				new TitleRequestDTO(null, "Odas", null, 2020, "FISICO", poetry, "Nueva", null, null, null),
				new TitleRequestDTO(null, "Odas", null, 2020, "FISICO", poetry, "Nueva", List.of("Ana", "ana"), null, null),
				new TitleRequestDTO(null, "Odas", null, 2020, "FISICO", poetry, "Nueva", List.of("a".repeat(151)), null, null),
				new TitleRequestDTO(null, "Odas", "12345", 2020, "FISICO", poetry, "Nueva", List.of("Ana"), null, null),
				new TitleRequestDTO(null, "Odas", "123456789012345678", 2020, "FISICO", poetry, "Nueva", List.of("Ana"), null, null),
				new TitleRequestDTO(null, "Odas", null, 999, "FISICO", poetry, "Nueva", List.of("Ana"), null, null),
				new TitleRequestDTO(null, "Odas", null, 2101, "FISICO", poetry, "Nueva", List.of("Ana"), null, null));
		for (final TitleRequestDTO request : invalidData) {
			assertEquals(MessageKey.BOOK_TITLE_DATA_INVALID, keyOf(() -> controller.registerTitle(request)), request.toString());
		}
		assertEquals(MessageKey.BOOK_TITLE_DATA_INVALID, keyOf(() -> controller.registerTitle(null)));
		assertEquals(MessageKey.CATEGORY_NOT_FOUND, keyOf(() -> controller.registerTitle(title(null, "Odas", "FISICO", null, List.of()))));
		assertEquals(MessageKey.CATEGORY_NOT_FOUND, keyOf(() -> controller.registerTitle(title(null, "Odas", "FISICO", UUID.randomUUID(), List.of()))));
		final List<TitleRequestDTO> invalidCopies = List.of(
				title(null, "Odas", "DIGITAL", poetry, List.of("PO-1")),
				new TitleRequestDTO(null, "Odas", null, 2020, "FISICO", poetry, "Nueva", List.of("Ana"), null, List.of("PO-1")),
				title(null, "Odas", "FISICO", poetry, List.of("PO-1", "PO-1")),
				title(null, "Odas", "FISICO", poetry, List.of("P".repeat(51))));
		for (final TitleRequestDTO request : invalidCopies) {
			assertEquals(MessageKey.BOOK_TITLE_COPIES_INVALID, keyOf(() -> controller.registerTitle(request)), request.toString());
		}
		assertTrue(jpa.books.isEmpty());
	}

	@Test
	@DisplayName("no deja repetir el ISBN de otro título, pero sí conservar el propio")
	void isbnIsUnique() {
		final UUID first = controller.registerTitle(withIsbn("9780000000002")).getBody().id();

		assertEquals(MessageKey.BOOK_TITLE_ISBN_TAKEN, keyOf(() -> controller.registerTitle(withIsbn("978-0000000002"))));
		final UUID second = controller.registerTitle(withIsbn("9780000000019")).getBody().id();
		assertEquals(MessageKey.BOOK_TITLE_ISBN_TAKEN, keyOf(() -> controller.updateTitle(second, withIsbn("9780000000002"))));
		assertEquals("9780000000002", controller.updateTitle(first, withIsbn("9780000000002")).getBody().isbn());
	}

	@Test
	@DisplayName("edita el título, cambia autores y categoría, y agrega ejemplares nuevos")
	void updatesTitle() {
		final UUID id = controller.registerTitle(title(null, "Odas", "FISICO", poetry, List.of())).getBody().id();

		final CatalogAdminResponseDTOs.Title updated = controller.updateTitle(id, new TitleRequestDTO(null, "Odas completas", null,
				1990, "FISICO", history, "Otra editorial", List.of("Beto"), BRANCH, List.of("OD-1"))).getBody();
		controller.updateTitle(id, new TitleRequestDTO(null, "Odas completas", null, 1990, "FISICO", history, "Otra editorial",
				List.of("Beto"), null, null));

		assertEquals("Odas completas", updated.title());
		assertEquals(history, updated.category());
		assertEquals(List.of("Beto"), updated.authors());
		assertEquals(1990, updated.year());
		assertEquals(1, registered.size());
		assertEquals(2, audited.stream().filter(line -> line.startsWith(AuditPort.TITLE_UPDATED)).count());
	}

	@Test
	@DisplayName("no edita títulos de baja, no cambia el tipo y no pasa a una categoría inactiva")
	void updateRules() {
		final UUID id = controller.registerTitle(title(null, "Odas", "FISICO", poetry, List.of())).getBody().id();
		controller.deactivateCategory(history);

		assertEquals(MessageKey.BOOK_TITLE_FORMAT_LOCKED, keyOf(() -> controller.updateTitle(id, title(null, "Odas", "DIGITAL", poetry, List.of()))));
		assertEquals(MessageKey.CATEGORY_INACTIVE, keyOf(() -> controller.updateTitle(id, title(null, "Odas", "FISICO", history, List.of()))));
		assertEquals(MessageKey.BOOK_TITLE_NOT_FOUND, keyOf(() -> controller.updateTitle(UUID.randomUUID(), title(null, "Odas", "FISICO", poetry, List.of()))));
		assertEquals(MessageKey.BOOK_TITLE_NOT_FOUND, keyOf(() -> controller.updateTitle(UUID.randomUUID(), null)));
		controller.withdrawTitle(id);
		assertEquals(MessageKey.BOOK_TITLE_WITHDRAWN, keyOf(() -> controller.updateTitle(id, title(null, "Odas", "FISICO", poetry, List.of()))));
	}

	@Test
	@DisplayName("dar de baja un título retira sus ejemplares y no se repite")
	void withdrawsTitle() {
		final UUID id = controller.registerTitle(title(null, "Odas", "FISICO", poetry, List.of())).getBody().id();

		final CatalogAdminResponseDTOs.Title withdrawn = controller.withdrawTitle(id).getBody();

		assertEquals("BAJA", withdrawn.status());
		assertEquals(List.of(id), retired);
		assertTrue(audited.contains(AuditPort.TITLE_WITHDRAWN + " " + INSTITUTION + " " + LIBRARIAN));
		assertEquals(MessageKey.BOOK_TITLE_WITHDRAWN, keyOf(() -> controller.withdrawTitle(id)));
		assertEquals(MessageKey.BOOK_TITLE_NOT_FOUND, keyOf(() -> controller.withdrawTitle(UUID.randomUUID())));
		assertEquals(MessageKey.BOOK_TITLE_NOT_FOUND, keyOf(() -> controller.title(UUID.randomUUID())));
	}

	@Test
	@DisplayName("si otro proceso da de baja el título al mismo tiempo, se avisa el conflicto")
	void withdrawRace() {
		final UUID id = controller.registerTitle(title(null, "Odas", "FISICO", poetry, List.of())).getBody().id();
		jpa.loseNextWithdraw = true;

		assertEquals(MessageKey.BOOK_TITLE_WITHDRAWN, keyOf(() -> controller.withdrawTitle(id)));
	}

	@Test
	@DisplayName("los registros del dominio normalizan y comparan sus valores")
	void domainRecords() {
		final CatalogAdminDomains.Category category = new CatalogAdminDomains.Category(null, " Arte ", null, -3);
		final CatalogAdminDomains.Title title = new CatalogAdminDomains.Title(null, null, null, -5, null, null, null, null, null, null, null);

		assertTrue(category.isActive());
		assertTrue(category.sameName(" ARTE"));
		assertFalse(category.sameName(null));
		assertEquals(0, category.activeTitles());
		assertFalse(new CatalogAdminDomains.Category(null, "Arte", "INACTIVA", 0).isActive());
		assertEquals(0, title.year());
		assertTrue(title.authors().isEmpty());
		assertFalse(title.isWithdrawn());
		assertTrue(title.withId(UUID.randomUUID(), "BAJA").isWithdrawn());
		assertEquals(new UUID(0, 0), new CatalogAdminDomains.CategoryStatusChange(null, false).id());
	}

	@Test
	@DisplayName("los casos de uso no se crean sin sus dependencias")
	void requiresDependencies() {
		assertThrows(BookHiveException.class, () -> new ListCategoriesUseCaseImpl(null));
		assertThrows(BookHiveException.class, () -> new FindTitleUseCaseImpl(null));
		assertThrows(BookHiveException.class, () -> new SaveCategoryUseCaseImpl(null, null, null));
		assertThrows(BookHiveException.class, () -> new ChangeCategoryStatusUseCaseImpl(null, null, null));
		assertThrows(BookHiveException.class, () -> new RegisterTitleUseCaseImpl(null, null, null, null, null));
		assertThrows(BookHiveException.class, () -> new UpdateTitleUseCaseImpl(null, null, null, null, null));
		assertThrows(BookHiveException.class, () -> new WithdrawTitleUseCaseImpl(null, null, null, null));
	}

	private TitleRequestDTO title(final UUID id, final String name, final String format, final UUID category, final List<String> barcodes) {
		return new TitleRequestDTO(id, name, null, 2020, format, category, "Nueva", List.of("Ana"), BRANCH, barcodes);
	}

	private TitleRequestDTO withIsbn(final String isbn) {
		return new TitleRequestDTO(null, "Con ISBN", isbn, 2020, "FISICO", poetry, "Nueva", List.of("Ana"), null, null);
	}

	private static String keyOf(final Supplier<?> call) {
		try {
			call.get();
			return "pasa";
		} catch (final BookHiveException exception) {
			return exception.getUserMessageKey();
		}
	}

	private static final class Jpa implements CatalogAdminJpaRepository {

		private final Map<UUID, String[]> categories = new LinkedHashMap<>();
		private final Map<UUID, Object[]> books = new LinkedHashMap<>();
		private final Map<UUID, String> publishers = new LinkedHashMap<>();
		private final Map<UUID, String> authors = new LinkedHashMap<>();
		private final Map<UUID, List<UUID>> bookAuthors = new LinkedHashMap<>();
		private boolean loseNextWithdraw;

		@Override
		public List<CategoryRow> findCategories() {
			return categories.entrySet().stream()
					.sorted((a, b) -> a.getValue()[0].toLowerCase(Locale.ROOT).compareTo(b.getValue()[0].toLowerCase(Locale.ROOT)))
					.map(entry -> (CategoryRow) new CategoryRow() {

						@Override
						public UUID getId() {
							return entry.getKey();
						}

						@Override
						public String getName() {
							return entry.getValue()[0];
						}

						@Override
						public String getStatus() {
							return entry.getValue()[1];
						}

						@Override
						public Long getTitles() {
							final long count = books.values().stream()
									.filter(book -> entry.getKey().equals(book[5]) && "ACTIVO".equals(book[4])).count();
							return count == 0 ? null : count;
						}

					}).toList();
		}

		@Override
		public int createCategory(final UUID id, final String name) {
			categories.put(id, new String[] { name, "ACTIVA" });
			return 1;
		}

		@Override
		public int renameCategory(final UUID id, final String name) {
			categories.get(id)[0] = name;
			return 1;
		}

		@Override
		public int changeCategoryStatus(final UUID id, final String status) {
			categories.get(id)[1] = status;
			return 1;
		}

		@Override
		public List<TitleRow> findTitle(final UUID id) {
			final Object[] book = books.get(id);
			if (book == null) {
				return List.of();
			}
			final String names = String.join(BookJpaRepository.AUTHOR_SEPARATOR,
					bookAuthors.getOrDefault(id, List.of()).stream().map(authors::get).sorted().toList());
			return List.of(new TitleRow() {

				@Override
				public UUID getId() {
					return id;
				}

				@Override
				public String getTitle() {
					return (String) book[0];
				}

				@Override
				public String getIsbn() {
					return (String) book[1];
				}

				@Override
				public Integer getYear() {
					return (Integer) book[2];
				}

				@Override
				public String getFormat() {
					return (String) book[3];
				}

				@Override
				public String getStatus() {
					return (String) book[4];
				}

				@Override
				public UUID getCategory() {
					return (UUID) book[5];
				}

				@Override
				public String getPublisher() {
					return publishers.get(book[6]);
				}

				@Override
				public String getAuthors() {
					return names.isEmpty() ? null : names;
				}

			});
		}

		@Override
		public long countIsbn(final String isbn, final UUID except) {
			return books.entrySet().stream().filter(entry -> isbn.equals(entry.getValue()[1]) && !entry.getKey().equals(except)).count();
		}

		@Override
		public List<UUID> findPublisher(final String name) {
			return publishers.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(name)).map(Map.Entry::getKey).toList();
		}

		@Override
		public int createPublisher(final UUID id, final String name) {
			publishers.put(id, name);
			return 1;
		}

		@Override
		public List<UUID> findAuthor(final String name) {
			return authors.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(name)).map(Map.Entry::getKey).toList();
		}

		@Override
		public int createAuthor(final UUID id, final String name) {
			authors.put(id, name);
			return 1;
		}

		@Override
		public int createBook(final UUID id, final String title, final String isbn, final int year, final String format,
				final UUID category, final UUID publisher) {
			books.put(id, new Object[] { title, isbn.isEmpty() ? null : isbn, year == 0 ? null : year, format, "ACTIVO", category, publisher });
			return 1;
		}

		@Override
		public int updateBook(final UUID id, final String title, final String isbn, final int year, final UUID category,
				final UUID publisher) {
			final Object[] book = books.get(id);
			book[0] = title;
			book[1] = isbn.isEmpty() ? null : isbn;
			book[2] = year == 0 ? null : year;
			book[5] = category;
			book[6] = publisher;
			return 1;
		}

		@Override
		public int deleteAuthors(final UUID book) {
			bookAuthors.remove(book);
			return 1;
		}

		@Override
		public int addAuthor(final UUID book, final UUID author) {
			bookAuthors.computeIfAbsent(book, key -> new ArrayList<>()).add(author);
			return 1;
		}

		@Override
		public int withdrawBook(final UUID id) {
			if (loseNextWithdraw) {
				return 0;
			}
			books.get(id)[4] = "BAJA";
			return 1;
		}

	}

}
