package co.com.bookhive.circulation.services.usecase.account.manageuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import co.com.bookhive.access.services.input.dto.request.ChangePersonRequestDTO;
import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;
import co.com.bookhive.circulation.infrastructure.input.controllers.UserLifecycleController;
import co.com.bookhive.circulation.infrastructure.input.tasks.UserTasksController;
import co.com.bookhive.circulation.infrastructure.output.externalservices.access.AccessPersonAdapter;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters.UserLifecycleRepositoryAdapter;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.UserLifecycleJpaRepository;
import co.com.bookhive.circulation.services.input.adapters.UserLifecycleInputAdapters;
import co.com.bookhive.circulation.services.input.dto.response.UserLifecycleResponseDTOs;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParameterChangeEntity;
import co.com.bookhive.circulation.services.usecase.account.changeuserstatus.impl.ChangeUserStatusUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.account.configureretention.impl.ConfigureRetentionUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.account.purgeexpiredusers.impl.PurgeExpiredUsersUseCaseImpl;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class UserLifecycleTest {

	private static final UUID INSTITUTION = UUID.randomUUID();
	private static final UUID LIBRARIAN = UUID.randomUUID();
	private static final UUID ANA = UUID.randomUUID();
	private static final UUID BETO = UUID.randomUUID();

	private Jpa jpa;
	private List<ChangePersonRequestDTO> people;
	private List<String> audited;
	private List<InstitutionParameterChangeEntity> changes;
	private UserLifecycleController controller;
	private PurgeExpiredUsersUseCaseImpl purge;

	@BeforeEach
	void prepare() {
		TenantContext.set("uco");
		jpa = new Jpa();
		jpa.users.put(ANA, new Object[] { UserLifecycleDomains.ACTIVE, null });
		jpa.users.put(BETO, new Object[] { UserLifecycleDomains.ACTIVE, null });
		people = new ArrayList<>();
		audited = new ArrayList<>();
		changes = new ArrayList<>();
		final UserLifecycleRepositoryAdapter users = new UserLifecycleRepositoryAdapter(jpa);
		final AccessPersonAdapter access = new AccessPersonAdapter(people::add);
		final SessionContextPort session = new SessionContextPort() {

			@Override
			public UUID getInstitution() {
				return INSTITUTION;
			}

			@Override
			public UUID getLibrarian() {
				return LIBRARIAN;
			}

		};
		final AuditPort audit = (action, institution, person, detail) -> audited.add(action);
		controller = new UserLifecycleController(
				UserLifecycleInputAdapters.changeStatus(new ChangeUserStatusUseCaseImpl(users, access, session, audit)),
				UserLifecycleInputAdapters.configureRetention(new ConfigureRetentionUseCaseImpl(users, changes::add, session, audit)),
				() -> ANA);
		purge = new PurgeExpiredUsersUseCaseImpl(users, access, audit);
	}

	@AfterEach
	void clear() {
		TenantContext.clear();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	@DisplayName("dar de baja deja al usuario RETIRADO con fecha e inactiva su persona")
	void deactivates() {
		final UserLifecycleResponseDTOs.Account account = controller.deactivate(ANA).getBody();
		controller.deactivate(ANA);

		assertEquals(UserLifecycleDomains.RETIRED, account.status());
		assertNotNull(account.deactivatedAt());
		assertEquals(1, jpa.statusChanges);
		assertEquals(List.of(new ChangePersonRequestDTO(ANA, "uco", ChangePersonRequestDTO.DEACTIVATE),
				new ChangePersonRequestDTO(ANA, "uco", ChangePersonRequestDTO.DEACTIVATE)), people);
		assertEquals(List.of(AuditPort.USER_DEACTIVATED, AuditPort.USER_DEACTIVATED), audited);
	}

	@Test
	@DisplayName("reactivar devuelve al usuario a ACTIVO y borra la fecha de baja")
	void reactivates() {
		controller.deactivate(ANA);

		final UserLifecycleResponseDTOs.Account account = controller.reactivate(ANA).getBody();

		assertEquals(UserLifecycleDomains.ACTIVE, account.status());
		assertNull(account.deactivatedAt());
		assertEquals(ChangePersonRequestDTO.REACTIVATE, people.get(1).action());
	}

	@Test
	@DisplayName("depurar anonimiza al usuario y a su persona, y repetirlo solo vuelve a anonimizar la persona")
	void purges() {
		final UserLifecycleResponseDTOs.Account account = controller.purge(BETO).getBody();
		final UserLifecycleResponseDTOs.Account again = controller.purge(BETO).getBody();

		assertEquals(UserLifecycleDomains.PURGED, account.status());
		assertEquals(UserLifecycleDomains.PURGED, again.status());
		assertEquals(2, people.stream().filter(request -> ChangePersonRequestDTO.ANONYMIZE.equals(request.action())).count());
		assertEquals(List.of(AuditPort.USER_PURGED), audited);
		assertEquals(MessageKey.USER_PURGED, keyOf(() -> controller.reactivate(BETO)));
	}

	@Test
	@DisplayName("el lector puede pedir que borren sus datos si no debe nada")
	void purgesOwnAccount() {
		assertEquals(UserLifecycleDomains.PURGED, controller.purgeMine().getBody().status());
	}

	@Test
	@DisplayName("no depura a quien tiene préstamos activos o multas pendientes")
	void refusesWhileOwing() {
		jpa.loans.put(ANA, 1L);
		assertEquals(MessageKey.USER_HAS_ACTIVE_LOANS, keyOf(() -> controller.purge(ANA)));
		jpa.loans.remove(ANA);
		jpa.fines.put(ANA, 2L);
		assertEquals(MessageKey.USER_HAS_PENDING_FINES, keyOf(() -> controller.purge(ANA)));
		assertEquals(MessageKey.USER_NOT_FOUND, keyOf(() -> controller.deactivate(UUID.randomUUID())));
		assertEquals(MessageKey.USER_NOT_FOUND, keyOf(() -> controller.deactivate(null)));
		assertEquals(UserLifecycleDomains.ACTIVE, jpa.users.get(ANA)[0]);
	}

	@Test
	@DisplayName("el tiempo de conservación se consulta, se cambia con historial y se valida")
	void configuresRetention() {
		assertEquals(24, controller.retention().getBody().months());
		jpa.retention = 12;
		assertEquals(12, controller.retention().getBody().months());

		assertEquals(36, controller.changeRetention(new UserLifecycleResponseDTOs.Retention(36)).getBody().months());
		controller.changeRetention(new UserLifecycleResponseDTOs.Retention(36));

		assertEquals(36, jpa.retention);
		assertEquals(1, changes.size());
		assertEquals("12", changes.get(0).previousValue());
		assertEquals("36", changes.get(0).newValue());
		assertEquals(List.of(AuditPort.INSTITUTION_CONFIGURED), audited);
		assertEquals(MessageKey.USER_RETENTION_INVALID, keyOf(() -> controller.changeRetention(new UserLifecycleResponseDTOs.Retention(0))));
		assertEquals(MessageKey.USER_RETENTION_INVALID, keyOf(() -> controller.changeRetention(new UserLifecycleResponseDTOs.Retention(121))));
		assertEquals(MessageKey.USER_RETENTION_INVALID, keyOf(() -> controller.changeRetention(null)));
	}

	@Test
	@DisplayName("la tarea depura solo a los retirados vencidos que no deben nada")
	void purgesExpired() {
		final UUID owing = UUID.randomUUID();
		final UUID reactivated = UUID.randomUUID();
		jpa.users.put(owing, new Object[] { UserLifecycleDomains.RETIRED, Instant.now() });
		jpa.users.put(reactivated, new Object[] { UserLifecycleDomains.ACTIVE, null });
		controller.deactivate(BETO);
		jpa.fines.put(owing, 1L);
		jpa.expired = List.of(BETO, owing, reactivated, UUID.randomUUID());
		people.clear();
		audited.clear();
		final List<String> institutions = List.of("uco", "rota");
		final UserTasksController tasks = new UserTasksController(() -> institutions, institution -> {
			if ("rota".equals(institution)) {
				throw BookHiveException.createUnexpected(MessageKey.REQUEST_UNEXPECTED, "falló");
			}
			return UserLifecycleInputAdapters.purgeExpired(purge).execute(institution);
		}, new Transactions());

		final UserLifecycleResponseDTOs.PurgeRuns runs = tasks.purgeExpired().getBody();

		assertEquals(List.of(new UserLifecycleResponseDTOs.PurgeRun("uco", 1, 3)), runs.institutions());
		assertEquals(UserLifecycleDomains.PURGED, jpa.users.get(BETO)[0]);
		assertEquals(List.of(AuditPort.USER_PURGED), audited);
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("dentro de una transacción el cambio de la persona espera a que se confirme")
	void waitsForCommit() {
		TransactionSynchronizationManager.initSynchronization();
		controller.deactivate(ANA);

		assertTrue(people.isEmpty());
		TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
		assertEquals(1, people.size());
	}

	@Test
	@DisplayName("los registros del dominio usan valores por defecto")
	void domainDefaults() {
		final UserLifecycleDomains.Request request = new UserLifecycleDomains.Request(null, null);
		final UserLifecycleDomains.Account account = new UserLifecycleDomains.Account(null, null, null);

		assertEquals(UserLifecycleDomains.Change.DEACTIVATE, request.change());
		assertEquals("", account.status());
		assertEquals(new UUID(0, 0), account.user());
		assertThrows(BookHiveException.class, request::validate);
		assertThrows(BookHiveException.class, () -> new ChangeUserStatusUseCaseImpl(null, null, null, null));
		assertThrows(BookHiveException.class, () -> new PurgeExpiredUsersUseCaseImpl(null, null, null));
		assertThrows(BookHiveException.class, () -> new ConfigureRetentionUseCaseImpl(null, null, null, null));
	}

	private static String keyOf(final Supplier<?> call) {
		try {
			call.get();
			return "pasa";
		} catch (final BookHiveException exception) {
			return exception.getUserMessageKey();
		}
	}

	private static final class Transactions implements PlatformTransactionManager {

		@Override
		public TransactionStatus getTransaction(final TransactionDefinition definition) {
			return new SimpleTransactionStatus();
		}

		@Override
		public void commit(final TransactionStatus status) {
			status.flush();
		}

		@Override
		public void rollback(final TransactionStatus status) {
			status.setRollbackOnly();
		}

	}

	private static final class Jpa implements UserLifecycleJpaRepository {

		private final Map<UUID, Object[]> users = new LinkedHashMap<>();
		private final Map<UUID, Long> loans = new LinkedHashMap<>();
		private final Map<UUID, Long> fines = new LinkedHashMap<>();
		private List<UUID> expired = List.of();
		private Integer retention;
		private int statusChanges;

		@Override
		public List<AccountRow> lock(final UUID id) {
			final Object[] user = users.get(id);
			if (user == null) {
				return List.of();
			}
			return List.of(new AccountRow() {

				@Override
				public UUID getId() {
					return id;
				}

				@Override
				public String getStatus() {
					return (String) user[0];
				}

				@Override
				public Instant getDeactivatedAt() {
					return (Instant) user[1];
				}

			});
		}

		@Override
		public long activeLoans(final UUID id) {
			return loans.getOrDefault(id, 0L);
		}

		@Override
		public long pendingFines(final UUID id) {
			return fines.getOrDefault(id, 0L);
		}

		@Override
		public int changeStatus(final UUID id, final String status) {
			statusChanges++;
			users.put(id, new Object[] { status, UserLifecycleDomains.RETIRED.equals(status) ? Instant.now() : null });
			return 1;
		}

		@Override
		public int anonymize(final UUID id) {
			users.get(id)[0] = UserLifecycleDomains.PURGED;
			return 1;
		}

		@Override
		public List<UUID> expiredInactive(final int months) {
			return expired;
		}

		@Override
		public List<Integer> retentionMonths() {
			return retention == null ? List.of() : List.of(retention);
		}

		@Override
		public int changeRetention(final int months) {
			retention = months;
			return 1;
		}

	}

}
