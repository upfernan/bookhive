package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class UserEntityTest {

	@Test
	@DisplayName("nace con valores por defecto y activo")
	void startsWithDefaults() {
		final UserEntity user = new UserEntity();

		assertEquals(UUIDHelper.getDefaultValue(), user.getId());
		assertEquals("", user.getName());
		assertEquals("", user.getEmail());
		assertEquals(UserEntity.STATUS_ACTIVE, user.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), user.getCreatedAt());
	}

	@Test
	@DisplayName("guarda los datos sin espacios en los extremos")
	void keepsTrimmedValues() {
		final UUID id = UUIDHelper.generateNew();
		final Instant createdAt = Instant.parse("2026-10-17T15:00:00Z");
		final UserEntity user = new UserEntity();

		user.setId(id);
		user.setName("  Lector de prueba  ");
		user.setEmail(" lector@bookhive.test ");
		user.setStatus(" " + UserEntity.STATUS_SUSPENDED + " ");
		user.setCreatedAt(createdAt);

		assertEquals(id, user.getId());
		assertEquals("Lector de prueba", user.getName());
		assertEquals("lector@bookhive.test", user.getEmail());
		assertEquals(UserEntity.STATUS_SUSPENDED, user.getStatus());
		assertEquals(createdAt, user.getCreatedAt());
	}

	@Test
	@DisplayName("convierte los nulos en valores por defecto")
	void replacesNulls() {
		final UserEntity user = new UserEntity();
		user.setStatus(UserEntity.STATUS_RETIRED);

		user.setId(null);
		user.setName(null);
		user.setEmail(null);
		user.setStatus(null);
		user.setCreatedAt(null);

		assertEquals(UUIDHelper.getDefaultValue(), user.getId());
		assertEquals("", user.getName());
		assertEquals("", user.getEmail());
		assertEquals(UserEntity.STATUS_ACTIVE, user.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), user.getCreatedAt());
	}

	@Test
	@DisplayName("un estado en blanco deja al usuario activo")
	void blankStatusIsActive() {
		final UserEntity user = new UserEntity();
		user.setStatus(UserEntity.STATUS_SUSPENDED);

		user.setStatus("   ");

		assertEquals(UserEntity.STATUS_ACTIVE, user.getStatus());
	}

}
