package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities;
import co.com.bookhive.access.testdata.ScriptedDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class RegistrationAndPersonAdaptersTest {

	private static final UUID UCO = UUID.randomUUID();
	private static final RegistrationEntities.DomainInstitution INSTITUTION = new RegistrationEntities.DomainInstitution(UCO, "uco");
	private static final RegistrationEntities.NewPerson CARLA =
			new RegistrationEntities.NewPerson(UUID.randomUUID(), "carla-id", "Carla", "carla@uco.edu.co", 1);

	@Test
	@DisplayName("busca el dominio, la identidad y el aviso vigente con la institución correcta")
	void readsDomainIdentityAndPolicy() {
		final OffsetDateTime published = OffsetDateTime.of(2026, 10, 20, 15, 4, 5, 0, ZoneOffset.UTC);
		final ScriptedDataSource db = new ScriptedDataSource()
				.rows("dominio_institucion", new Object[] { UCO, "uco" })
				.rows("WHERE identidad", new Object[] { UUID.randomUUID() })
				.rows("ORDER BY version DESC", new Object[] { 2, "Aviso", published });
		final RegistrationRepositoryAdapter adapter = new RegistrationRepositoryAdapter(db.dataSource());

		assertEquals(INSTITUTION, adapter.institutionOfDomain("uco.edu.co").orElseThrow());
		assertTrue(adapter.identityRegistered("carla-id"));
		assertEquals(new RegistrationEntities.PublicPolicy(2, "Aviso", published.toInstant()), adapter.currentPolicyOf(INSTITUTION).orElseThrow());
		assertEquals(List.of("[inst_uco]"), db.calls("set_config('role'"));
		assertTrue(db.transactions().contains("rollback"));
	}

	@Test
	@DisplayName("sin filas no hay dominio, identidad ni aviso")
	void emptyLookups() {
		final RegistrationRepositoryAdapter adapter = new RegistrationRepositoryAdapter(new ScriptedDataSource().dataSource());

		assertTrue(adapter.institutionOfDomain("gmail.com").isEmpty());
		assertFalse(adapter.identityRegistered("nadie"));
		assertTrue(adapter.currentPolicyOf(INSTITUTION).isEmpty());
	}

	@Test
	@DisplayName("registra en una sola transacción persona, usuario, rol LECTOR y aceptación del aviso")
	void registersInOneTransaction() {
		final ScriptedDataSource db = new ScriptedDataSource().rows("coalesce(max(version)", new Object[] { 1 });

		final RegistrationEntities.NewPerson registered = new RegistrationRepositoryAdapter(db.dataSource()).register(CARLA, INSTITUTION);

		assertEquals(CARLA, registered);
		assertEquals(1, db.calls("INSERT INTO compartido.persona").size());
		assertEquals(1, db.calls("INSERT INTO library_user").size());
		assertEquals(List.of("[" + CARLA.id() + ", LECTOR]"), db.calls("INSERT INTO person_role"));
		assertEquals(List.of("[" + CARLA.id() + ", 1]"), db.calls("INSERT INTO data_policy_acceptance"));
		assertTrue(db.transactions().contains("commit"));
	}

	@Test
	@DisplayName("en una universidad creada por autorregistro el primero queda como ADMINISTRADOR y los demás como LECTOR")
	void firstPersonOfSelfRegisteredInstitutionAdministers() {
		final ScriptedDataSource first = new ScriptedDataSource()
				.rows("coalesce(max(version)", new Object[] { 1 })
				.rows("alta = 'AUTOREGISTRO'", new Object[] { UCO });
		final ScriptedDataSource second = new ScriptedDataSource()
				.rows("coalesce(max(version)", new Object[] { 1 })
				.rows("alta = 'AUTOREGISTRO'", new Object[] { UCO })
				.rows("r.name = 'ADMINISTRADOR'", new Object[] { UUID.randomUUID() });

		new RegistrationRepositoryAdapter(first.dataSource()).register(CARLA, INSTITUTION);
		new RegistrationRepositoryAdapter(second.dataSource()).register(CARLA, INSTITUTION);

		assertEquals(List.of("[" + CARLA.id() + ", ADMINISTRADOR]"), first.calls("INSERT INTO person_role"));
		assertEquals(List.of("[" + CARLA.id() + ", LECTOR]"), second.calls("INSERT INTO person_role"));
	}

	@Test
	@DisplayName("el aviso base sale del catálogo de mensajes como versión 1")
	void readsDefaultPolicy() {
		final RegistrationRepositoryAdapter adapter = new RegistrationRepositoryAdapter(
				new ScriptedDataSource().rows("data-policy.default", new Object[] { "Aviso base" }).dataSource());

		assertEquals(new RegistrationEntities.PublicPolicy(1, "Aviso base", null), adapter.defaultPolicy().orElseThrow());
		assertTrue(new RegistrationRepositoryAdapter(new ScriptedDataSource().dataSource()).defaultPolicy().isEmpty());
		assertThrows(BookHiveException.class, () -> new RegistrationRepositoryAdapter(
				new ScriptedDataSource().fails("data-policy.default", "08006").dataSource()).defaultPolicy());
	}

	@Test
	@DisplayName("une la cuenta a un usuario que la biblioteca ya tenía cargado con ese correo")
	void linksPreloadedUser() {
		final UUID preloaded = UUID.randomUUID();
		final ScriptedDataSource db = new ScriptedDataSource()
				.rows("coalesce(max(version)", new Object[] { 1 })
				.rows("FROM library_user WHERE lower(email)", new Object[] { preloaded });

		final RegistrationEntities.NewPerson registered = new RegistrationRepositoryAdapter(db.dataSource()).register(CARLA, INSTITUTION);

		assertEquals(preloaded, registered.id());
		assertTrue(db.calls("INSERT INTO library_user").isEmpty());
	}

	@Test
	@DisplayName("no registra con un aviso viejo, con el correo ya usado ni si la base falla, y deshace todo")
	void rollsBackOnFailure() {
		final UUID preloaded = UUID.randomUUID();
		final ScriptedDataSource outdated = new ScriptedDataSource().rows("coalesce(max(version)", new Object[] { 2 });
		final ScriptedDataSource taken = new ScriptedDataSource()
				.rows("coalesce(max(version)", new Object[] { 1 })
				.rows("FROM library_user WHERE lower(email)", new Object[] { preloaded })
				.rows("WHERE id = ?", new Object[] { preloaded });
		final ScriptedDataSource duplicated = new ScriptedDataSource()
				.rows("coalesce(max(version)", new Object[] { 1 })
				.fails("INSERT INTO compartido.persona", "23505");
		final ScriptedDataSource broken = new ScriptedDataSource()
				.rows("coalesce(max(version)", new Object[] { 1 })
				.fails("INSERT INTO compartido.persona", "08006");

		assertEquals(MessageKey.DATA_POLICY_VERSION_OUTDATED, keyOf(outdated));
		assertTrue(outdated.calls("INSERT INTO").isEmpty());
		assertEquals(MessageKey.REGISTRATION_ALREADY_REGISTERED, keyOf(taken));
		assertEquals(MessageKey.REGISTRATION_ALREADY_REGISTERED, keyOf(duplicated));
		assertEquals(MessageKey.REQUEST_UNEXPECTED, keyOf(broken));
		assertTrue(broken.transactions().contains("rollback"));
		assertEquals(MessageKey.DATA_POLICY_VERSION_OUTDATED, keyOf(new ScriptedDataSource()));
	}

	@Test
	@DisplayName("las fallas de la base en las búsquedas son errores inesperados")
	void lookupFailures() {
		final RegistrationRepositoryAdapter adapter = new RegistrationRepositoryAdapter(new ScriptedDataSource()
				.fails("dominio_institucion", "08006").fails("identidad", "08006").fails("data_policy", "08006").dataSource());

		assertThrows(BookHiveException.class, () -> adapter.institutionOfDomain("uco.edu.co"));
		assertThrows(BookHiveException.class, () -> adapter.identityRegistered("carla-id"));
		assertThrows(BookHiveException.class, () -> adapter.currentPolicyOf(INSTITUTION));
	}

	@Test
	@DisplayName("cambia el estado o anonimiza la persona solo dentro de su institución")
	void changesPersonStatus() {
		final UUID person = UUID.randomUUID();
		final ScriptedDataSource db = new ScriptedDataSource();
		final PersonStatusRepositoryAdapter adapter = new PersonStatusRepositoryAdapter(db.dataSource());

		adapter.changeStatus(person, "uco", "INACTIVA");
		adapter.anonymize(person, "uco");

		assertEquals(List.of("[INACTIVA, " + person + ", uco]"), db.calls("SET estado = ?"));
		assertEquals(List.of("[" + person + ", uco]"), db.calls("identidad = 'depurado-'"));
		assertThrows(BookHiveException.class, () -> adapter.anonymize(person, "uco; drop"));
		final PersonStatusRepositoryAdapter failing =
				new PersonStatusRepositoryAdapter(new ScriptedDataSource().fails("compartido.persona", "08006").dataSource());
		assertThrows(BookHiveException.class, () -> failing.changeStatus(person, "uco", "ACTIVA"));
	}

	private static String keyOf(final ScriptedDataSource db) {
		try {
			new RegistrationRepositoryAdapter(db.dataSource()).register(CARLA, INSTITUTION);
			return "pasa";
		} catch (final BookHiveException exception) {
			return exception.getUserMessageKey();
		}
	}

}
