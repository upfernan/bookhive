package co.com.bookhive.access.infrastructure.output.externalservices.provisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.testdata.ScriptedDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FlywayInstitutionProvisioningAdapterTest {

	private static final String URL = "jdbc:bookhive-guion:prueba";

	private ScriptedDriver driver;

	@AfterEach
	void unregister() throws SQLException {
		if (driver != null) {
			DriverManager.deregisterDriver(driver);
		}
	}

	@Test
	@DisplayName("el esquema y el nombre de la universidad nueva salen del dominio")
	void namesFromDomain() {
		assertEquals("upb_edu_co", FlywayInstitutionProvisioningAdapter.schemaOf("upb.edu.co"));
		assertEquals("i_3m_edu", FlywayInstitutionProvisioningAdapter.schemaOf("3m.edu"));
		assertEquals("i_", FlywayInstitutionProvisioningAdapter.schemaOf(null));
		assertEquals("u_ca_edu_co", FlywayInstitutionProvisioningAdapter.schemaOf("u-ca.edu.co"));
		assertEquals(60, FlywayInstitutionProvisioningAdapter.schemaOf("a".repeat(80) + ".edu").length());
		assertEquals("UPB", FlywayInstitutionProvisioningAdapter.nameOf("upb.edu.co"));
	}

	@Test
	@DisplayName("sin el usuario que crea instituciones no intenta nada y avisa que no está disponible")
	void requiresProvisioningUser() {
		final FlywayInstitutionProvisioningAdapter withoutUser = new FlywayInstitutionProvisioningAdapter(URL, "", "", "bookhive_app");
		final FlywayInstitutionProvisioningAdapter withoutPassword = new FlywayInstitutionProvisioningAdapter(URL, "prov", "", "bookhive_app");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> withoutUser.provision("upb.edu.co"));
		assertEquals(MessageKey.REGISTRATION_INSTITUTION_UNAVAILABLE, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNAVAILABLE, exception.getKind());
		assertThrows(BookHiveException.class, () -> withoutPassword.provision("upb.edu.co"));
	}

	@Test
	@DisplayName("una universidad nueva se aparta con un esquema libre y su dominio en una sola transacción")
	void reservesNewInstitution() throws SQLException {
		final ScriptedDataSource db = new ScriptedDataSource()
				.answer("WHERE esquema = ?", values -> "upb_edu_co".equals(values.get(0)) ? new Object[][] { { 1 } } : new Object[0][]);

		assertUnavailable(db);

		assertEquals(1, db.calls("INSERT INTO compartido.institucion").size());
		assertTrue(db.calls("INSERT INTO compartido.institucion").get(0).contains("UPB, upb_edu_co_2"));
		assertEquals(1, db.calls("INSERT INTO compartido.dominio_institucion").size());
		assertTrue(db.transactions().contains("commit"));
	}

	@Test
	@DisplayName("si la universidad ya está apartada no la crea otra vez")
	void reusesReservedInstitution() throws SQLException {
		final ScriptedDataSource db = new ScriptedDataSource()
				.rows("WHERE d.dominio = ?", new Object[] { UUID.randomUUID(), "upb_edu_co" });

		assertUnavailable(db);

		assertTrue(db.calls("INSERT INTO").isEmpty());
	}

	@Test
	@DisplayName("si otra persona apartó la misma universidad al mismo tiempo, usa la suya")
	void raceUsesOtherReservation() throws SQLException {
		final AtomicInteger searches = new AtomicInteger();
		final ScriptedDataSource db = new ScriptedDataSource()
				.answer("WHERE d.dominio = ?", values -> searches.incrementAndGet() == 1
						? new Object[0][]
						: new Object[][] { { UUID.randomUUID(), "upb_edu_co" } })
				.fails("INSERT INTO compartido.dominio_institucion", "23505");

		assertUnavailable(db);

		assertEquals(2, searches.get());
		assertTrue(db.transactions().contains("rollback"));
	}

	@Test
	@DisplayName("si falla el apartado de la universidad se deshace y avisa que no está disponible")
	void failedReservationRollsBack() throws SQLException {
		final ScriptedDataSource duplicated = new ScriptedDataSource().fails("INSERT INTO compartido.dominio_institucion", "23505");
		final ScriptedDataSource broken = new ScriptedDataSource().fails("INSERT INTO compartido.institucion", "08006");

		assertUnavailable(duplicated);
		assertUnavailable(broken);

		assertEquals(List.of("rollback"), broken.transactions());
	}

	private void assertUnavailable(final ScriptedDataSource db) throws SQLException {
		unregister();
		driver = new ScriptedDriver(db);
		DriverManager.registerDriver(driver);
		final FlywayInstitutionProvisioningAdapter adapter = new FlywayInstitutionProvisioningAdapter(URL, "prov", "clave", "bookhive_app");

		assertEquals(MessageKey.REGISTRATION_INSTITUTION_UNAVAILABLE,
				assertThrows(BookHiveException.class, () -> adapter.provision("upb.edu.co")).getUserMessageKey());
	}

	private static final class ScriptedDriver implements Driver {

		private final ScriptedDataSource db;

		ScriptedDriver(final ScriptedDataSource db) {
			this.db = db;
		}

		@Override
		public Connection connect(final String url, final Properties info) throws SQLException {
			return acceptsURL(url) ? db.dataSource().getConnection() : null;
		}

		@Override
		public boolean acceptsURL(final String url) {
			return url != null && url.startsWith(URL);
		}

		@Override
		public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
			return new DriverPropertyInfo[0];
		}

		@Override
		public int getMajorVersion() {
			return 1;
		}

		@Override
		public int getMinorVersion() {
			return 0;
		}

		@Override
		public boolean jdbcCompliant() {
			return false;
		}

		@Override
		public Logger getParentLogger() throws SQLFeatureNotSupportedException {
			throw new SQLFeatureNotSupportedException();
		}

	}

}
