package co.com.bookhive.circulation.services.usecase.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.com.bookhive.circulation.services.input.dto.request.RegisterBookCopiesRequestDTO;
import co.com.bookhive.circulation.services.input.ports.RegisterBookCopiesInputPort;
import co.com.bookhive.circulation.services.input.ports.RetireBookCopiesInputPort;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.usecase.location.registerbookcopies.impl.RegisterBookCopiesUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.retirebookcopies.impl.RetireBookCopiesUseCaseImpl;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.circulation.services.input.adapters.LocationInputAdapters;
import co.com.bookhive.circulation.infrastructure.input.controllers.LocationController;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters.LocationRepositoryAdapter;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LocationJpaRepository;
import co.com.bookhive.circulation.services.input.adapters.LocationInputAdapters;
import co.com.bookhive.circulation.services.input.dto.request.BranchRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.LocationLevelsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.RelocateBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.BranchEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.LevelEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.ValueEntity;
import co.com.bookhive.circulation.services.usecase.location.configurelocationlevels.impl.ConfigureLocationLevelsUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.createbranch.impl.CreateBranchUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.findlocationlevels.impl.FindLocationLevelsUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.listbranches.impl.ListBranchesUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.relocatebookcopy.impl.RelocateBookCopyUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.withdrawbookcopy.impl.WithdrawBookCopyUseCaseImpl;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class LocationUseCasesTest {

	private static final UUID CENTRO = UUID.randomUUID();
	private static final UUID NORTE = UUID.randomUUID();
	private static final String BARCODE = "UCO-CC-01";

	private final Locations locations = new Locations();
	private final FakeRepositories.BookCopies copies = new FakeRepositories.BookCopies();
	private final List<String> audited = new ArrayList<>();
	private final BookCopyEntity copy = new BookCopyEntity();

	LocationUseCasesTest() {
		locations.branches.add(new BranchEntity(CENTRO, "Centro"));
		locations.branches.add(new BranchEntity(NORTE, "Norte"));
		locations.levels.addAll(List.of(new LevelEntity(1, "Piso"), new LevelEntity(2, "Estante"), new LevelEntity(3, "Posición")));
		copy.setId(UUID.randomUUID());
		copy.setBarcode(BARCODE);
		copy.setBranch(CENTRO);
		copies.with(copy);
	}

	@Test
	@DisplayName("el bibliotecario ve la ubicación completa y la cambia cuando trasladan el ejemplar")
	void relocatesBookCopy() {
		final LocationController controller = controller();

		final var moved = controller.relocate(BARCODE, new RelocateBookCopyRequestDTO(null, NORTE, List.of("2", "B", "14")));
		final LocationResponseDTOs.BookCopyLocation found = controller.location(BARCODE).getBody();

		assertEquals(HttpStatus.OK, moved.getStatusCode());
		assertEquals("Norte / Piso 2 / Estante B / Posición 14", found.description());
		assertEquals(new LocationResponseDTOs.Spot(2, "Estante", "B"), found.spots().get(1));
		assertEquals(NORTE, locations.branchOf.get(copy.getId()));
		assertEquals(List.of(AuditPort.BOOK_COPY_RELOCATED + " UCO-CC-01: Centro -> Norte / Piso 2 / Estante B / Posición 14"), audited);
	}

	@Test
	@DisplayName("la ubicación se llena en orden, sin vacíos, y sin pasar los niveles de la institución")
	void rejectsInvalidLocation() {
		final LocationController controller = controller();
		final RelocateBookCopyRequestDTO tooDeep = new RelocateBookCopyRequestDTO(null, NORTE, List.of("1", "2", "3", "4"));
		final RelocateBookCopyRequestDTO blank = new RelocateBookCopyRequestDTO(null, NORTE, List.of("1", " "));
		final RelocateBookCopyRequestDTO unknownBranch = new RelocateBookCopyRequestDTO(null, UUID.randomUUID(), List.of("1"));
		final RelocateBookCopyRequestDTO noBranch = new RelocateBookCopyRequestDTO(null, null, List.of("1"));

		assertEquals(MessageKey.BOOK_COPY_LOCATION_INVALID, keyOf(() -> controller.relocate(BARCODE, tooDeep)));
		assertEquals(MessageKey.BOOK_COPY_LOCATION_INVALID, keyOf(() -> controller.relocate(BARCODE, blank)));
		assertEquals(MessageKey.BRANCH_NOT_FOUND, keyOf(() -> controller.relocate(BARCODE, unknownBranch)));
		assertEquals(MessageKey.BRANCH_NOT_FOUND, keyOf(() -> controller.relocate(BARCODE, noBranch)));
		assertEquals(MessageKey.BOOK_COPY_NOT_FOUND, keyOf(() -> controller.location("NO-EXISTE")));
		assertEquals(MessageKey.BOOK_COPY_BARCODE_REQUIRED, keyOf(() -> controller.location(" ")));
		assertEquals(MessageKey.BOOK_COPY_BARCODE_REQUIRED, keyOf(() -> controller.relocate(" ", null)));
		assertTrue(locations.values.isEmpty());
	}

	@Test
	@DisplayName("dar de baja un ejemplar disponible borra su ubicación; prestado o ya de baja no se puede")
	void withdrawsAvailableCopy() {
		final LocationController controller = controller();
		controller.relocate(BARCODE, new RelocateBookCopyRequestDTO(null, CENTRO, List.of("1")));

		final LocationResponseDTOs.BookCopyLocation withdrawn = controller.withdraw(BARCODE).getBody();

		assertEquals(BookCopyEntity.STATUS_WITHDRAWN, withdrawn.status());
		assertTrue(withdrawn.spots().isEmpty());
		assertEquals(MessageKey.BOOK_COPY_NOT_WITHDRAWABLE, keyOf(() -> controller.withdraw(BARCODE)));
		assertEquals(MessageKey.BOOK_COPY_WITHDRAWN,
				keyOf(() -> controller.relocate(BARCODE, new RelocateBookCopyRequestDTO(null, CENTRO, List.of()))));
		assertTrue(audited.get(1).startsWith(AuditPort.BOOK_COPY_WITHDRAWN + " UCO-CC-01 estaba en Centro / Piso 1"));
	}

	@Test
	@DisplayName("la institución define sus propios niveles, pero no puede quitar uno en el que hay ejemplares")
	void configuresLevels() {
		final LocationController controller = controller();

		final var levels = controller.configureLevels(new LocationLevelsRequestDTO(List.of("Salón", "Estante"))).getBody();
		controller.relocate(BARCODE, new RelocateBookCopyRequestDTO(null, CENTRO, List.of("A", "3")));

		assertEquals(List.of(new LocationResponseDTOs.Level(1, "Salón"), new LocationResponseDTOs.Level(2, "Estante")), levels);
		assertEquals(levels, controller.levels().getBody());
		assertEquals(MessageKey.LOCATION_LEVELS_IN_USE,
				keyOf(() -> controller.configureLevels(new LocationLevelsRequestDTO(List.of("Salón")))));
		assertEquals(MessageKey.LOCATION_LEVELS_INVALID,
				keyOf(() -> controller.configureLevels(new LocationLevelsRequestDTO(List.of("Piso", "piso")))));
		assertEquals(MessageKey.LOCATION_LEVELS_INVALID, keyOf(() -> controller.configureLevels(null)));
		assertEquals(MessageKey.LOCATION_LEVELS_INVALID, keyOf(() -> controller.configureLevels(
				new LocationLevelsRequestDTO(List.of("1", "2", "3", "4", "5", "6", "7")))));
	}

	@Test
	@DisplayName("crea sedes con nombre único y las lista")
	void createsBranches() {
		final LocationController controller = controller();

		final var created = controller.createBranch(new BranchRequestDTO("  Sur "));

		assertEquals(HttpStatus.CREATED, created.getStatusCode());
		assertEquals("Sur", created.getBody().name());
		assertEquals(3, controller.branches().getBody().size());
		assertEquals(MessageKey.BRANCH_NAME_TAKEN, keyOf(() -> controller.createBranch(new BranchRequestDTO("centro"))));
		assertEquals(MessageKey.BRANCH_NAME_INVALID, keyOf(() -> controller.createBranch(new BranchRequestDTO(""))));
		assertEquals(MessageKey.BRANCH_NAME_INVALID, keyOf(() -> controller.createBranch(null)));
		assertEquals(MessageKey.BRANCH_NAME_INVALID, keyOf(() -> controller.createBranch(new BranchRequestDTO("x".repeat(101)))));
	}

	@Test
	@DisplayName("los casos de uso no se crean sin sus dependencias")
	void requireDependencies() {
		assertThrows(BookHiveException.class, () -> new ListBranchesUseCaseImpl(null));
		assertThrows(BookHiveException.class, () -> new FindLocationLevelsUseCaseImpl(null));
		assertThrows(BookHiveException.class, () -> new FindBookCopyLocationUseCaseImpl(copies, null));
	}

	@Test
	@DisplayName("el adaptador guarda los niveles y la ubicación en la base")
	void adapterUsesRepository() {
		final List<String> calls = new ArrayList<>();
		final LocationJpaRepository jpa = (LocationJpaRepository) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { LocationJpaRepository.class }, (proxy, method, arguments) -> {
					calls.add(method.getName());
					final Class<?> type = method.getReturnType();
					if (List.class.equals(type)) {
						return List.of();
					}
					return long.class.equals(type) ? (Object) 0L : (Object) 1;
				});
		final LocationRepositoryAdapter adapter = new LocationRepositoryAdapter(jpa);

		adapter.replaceLevels(List.of(new LevelEntity(1, "Piso")));
		adapter.relocate(copy.getId(), CENTRO, List.of(new ValueEntity(1, "2")));
		adapter.createBranch(new BranchEntity(NORTE, "Norte"));
		adapter.clearValues(copy.getId());

		assertTrue(adapter.findBranches().isEmpty() && adapter.findLevels().isEmpty() && adapter.findValues(copy.getId()).isEmpty());
		assertEquals(false, adapter.hasValuesDeeperThan(2));
		assertEquals(List.of("deleteLevelsDeeperThan", "saveLevel", "changeBranch", "deleteValues", "saveValue", "createBranch",
				"deleteValues", "findBranches", "findLevels", "findValues", "countValuesDeeperThan"), calls);
	}

	@Test
	@DisplayName("registra ejemplares nuevos en una sede existente sin repetir códigos")
	void registersBookCopies() {
		final UUID book = UUID.randomUUID();
		final RegisterBookCopiesInputPort register = LocationInputAdapters.registerCopies(new RegisterBookCopiesUseCaseImpl(locations));

		assertEquals(2, register.execute(new RegisterBookCopiesRequestDTO(book, CENTRO, List.of(" N-1 ", "N-2"))));
		assertEquals(List.of("N-1", "N-2"), locations.added);
		assertEquals(MessageKey.BOOK_COPY_BARCODE_TAKEN, keyOf(() -> register.execute(new RegisterBookCopiesRequestDTO(book, CENTRO, List.of("N-1")))));
		assertEquals(MessageKey.BRANCH_NOT_FOUND, keyOf(() -> register.execute(new RegisterBookCopiesRequestDTO(book, UUID.randomUUID(), List.of("N-3")))));
		assertEquals(MessageKey.BOOK_TITLE_COPIES_INVALID, keyOf(() -> register.execute(new RegisterBookCopiesRequestDTO(book, CENTRO, List.of("N-4", "N-4")))));
		assertEquals(MessageKey.BOOK_TITLE_COPIES_INVALID, keyOf(() -> register.execute(new RegisterBookCopiesRequestDTO(book, CENTRO, List.of(" ")))));
		assertEquals(MessageKey.BOOK_TITLE_COPIES_INVALID, keyOf(() -> register.execute(new RegisterBookCopiesRequestDTO(book, CENTRO, List.of("X".repeat(51))))));
		assertEquals(MessageKey.BOOK_TITLE_COPIES_INVALID, keyOf(() -> register.execute(new RegisterBookCopiesRequestDTO(null, CENTRO, List.of("N-5")))));
		assertEquals(MessageKey.BOOK_TITLE_COPIES_INVALID, keyOf(() -> register.execute(null)));
		assertThrows(BookHiveException.class, () -> new RegisterBookCopiesUseCaseImpl(null));
	}

	@Test
	@DisplayName("retira todos los ejemplares de un título solo si están disponibles y nadie espera el libro")
	void retiresBookCopies() {
		final UUID book = UUID.randomUUID();
		final BookCopyEntity first = copyOf(book, "R-1", BookCopyEntity.STATUS_AVAILABLE);
		final BookCopyEntity second = copyOf(book, "R-2", BookCopyEntity.STATUS_AVAILABLE);
		copies.with(first).with(second);
		locations.values.put(first.getId(), new ArrayList<>());
		final FakeRepositories.Reservations reservations = new FakeRepositories.Reservations();
		final RetireBookCopiesInputPort retire =
				LocationInputAdapters.retireCopies(new RetireBookCopiesUseCaseImpl(copies, reservations, locations));

		assertEquals(2, retire.execute(book));
		assertEquals(BookCopyEntity.STATUS_WITHDRAWN, first.getStatus());
		assertTrue(locations.values.isEmpty());
		assertEquals(0, retire.execute(UUID.randomUUID()));
		assertEquals(MessageKey.BOOK_TITLE_NOT_FOUND, keyOf(() -> retire.execute(null)));
		assertThrows(BookHiveException.class, () -> new RetireBookCopiesUseCaseImpl(null, null, null));
	}

	@Test
	@DisplayName("no retira ejemplares prestados, con reservas en espera o que cambian a la vez")
	void refusesToRetireBusyCopies() {
		final UUID lent = UUID.randomUUID();
		final UUID awaited = UUID.randomUUID();
		final UUID racing = UUID.randomUUID();
		copies.with(copyOf(lent, "L-1", BookCopyEntity.STATUS_LENT)).with(copyOf(awaited, "W-1", BookCopyEntity.STATUS_AVAILABLE));
		final ReservationEntity waiting = new ReservationEntity();
		waiting.setBook(awaited);
		final FakeRepositories.Reservations reservations = new FakeRepositories.Reservations().with(waiting);
		final RetireBookCopiesInputPort retire =
				LocationInputAdapters.retireCopies(new RetireBookCopiesUseCaseImpl(copies, reservations, locations));
		final FakeRepositories.BookCopies changing = new FakeRepositories.BookCopies()
				.with(copyOf(racing, "C-1", BookCopyEntity.STATUS_AVAILABLE)).changedByOtherRequest();
		final RetireBookCopiesInputPort retireRacing =
				LocationInputAdapters.retireCopies(new RetireBookCopiesUseCaseImpl(changing, reservations, locations));

		assertEquals(MessageKey.BOOK_TITLE_COPIES_IN_USE, keyOf(() -> retire.execute(lent)));
		assertEquals(MessageKey.BOOK_TITLE_RESERVATIONS_PENDING, keyOf(() -> retire.execute(awaited)));
		assertEquals(MessageKey.BOOK_TITLE_COPIES_IN_USE, keyOf(() -> retireRacing.execute(racing)));
	}

	private static BookCopyEntity copyOf(final UUID book, final String barcode, final String status) {
		final BookCopyEntity entity = new BookCopyEntity();
		entity.setId(UUID.randomUUID());
		entity.setBook(book);
		entity.setBarcode(barcode);
		entity.setStatus(status);
		return entity;
	}

	private static String keyOf(final java.util.function.Supplier<?> call) {
		try {
			call.get();
			return "pasa";
		} catch (final BookHiveException exception) {
			return exception.getUserMessageKey();
		}
	}

	private LocationController controller() {
		final SessionContextPort session = new SessionContextPort() {

			@Override
			public UUID getInstitution() {
				return UUID.randomUUID();
			}

			@Override
			public UUID getLibrarian() {
				return UUID.randomUUID();
			}

		};
		final AuditPort audit = (action, institution, person, detail) -> audited.add(action + " " + detail);
		return new LocationController(new LocationController.Ports(
				LocationInputAdapters.findLevels(new FindLocationLevelsUseCaseImpl(locations)),
				LocationInputAdapters.configureLevels(new ConfigureLocationLevelsUseCaseImpl(locations, session, audit)),
				LocationInputAdapters.listBranches(new ListBranchesUseCaseImpl(locations)),
				LocationInputAdapters.createBranch(new CreateBranchUseCaseImpl(locations, session, audit)),
				LocationInputAdapters.findLocation(new FindBookCopyLocationUseCaseImpl(copies, locations)),
				LocationInputAdapters.relocate(new RelocateBookCopyUseCaseImpl(copies, locations, session, audit)),
				LocationInputAdapters.withdraw(new WithdrawBookCopyUseCaseImpl(copies, locations, session, audit))));
	}

	private static final class Locations implements LocationRepository {

		private final List<BranchEntity> branches = new ArrayList<>();
		private final List<LevelEntity> levels = new ArrayList<>();
		private final Map<UUID, List<ValueEntity>> values = new HashMap<>();
		private final Map<UUID, UUID> branchOf = new HashMap<>();
		private final List<String> added = new ArrayList<>();

		@Override
		public List<BranchEntity> findBranches() {
			return List.copyOf(branches);
		}

		@Override
		public void createBranch(final BranchEntity branch) {
			branches.add(branch);
		}

		@Override
		public List<LevelEntity> findLevels() {
			return List.copyOf(levels);
		}

		@Override
		public void replaceLevels(final List<LevelEntity> newLevels) {
			levels.clear();
			levels.addAll(newLevels);
		}

		@Override
		public boolean hasValuesDeeperThan(final int depth) {
			return values.values().stream().flatMap(List::stream).anyMatch(value -> value.depth() > depth);
		}

		@Override
		public List<ValueEntity> findValues(final UUID bookCopy) {
			return values.getOrDefault(bookCopy, List.of());
		}

		@Override
		public void relocate(final UUID bookCopy, final UUID branch, final List<ValueEntity> newValues) {
			branchOf.put(bookCopy, branch);
			values.put(bookCopy, List.copyOf(newValues));
		}

		@Override
		public void clearValues(final UUID bookCopy) {
			values.remove(bookCopy);
		}

		@Override
		public boolean anyBarcodeTaken(final List<String> barcodes) {
			return barcodes.stream().anyMatch(added::contains);
		}

		@Override
		public void addBookCopies(final UUID book, final UUID branch, final List<String> barcodes) {
			added.addAll(barcodes);
		}

	}

}
