package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class UserJpaEntityTest {

	@Test
	@DisplayName("nace con valores por defecto y activo")
	void startsWithDefaults() {
		final UserJpaEntity user = new UserJpaEntity();

		assertEquals(UUIDHelper.getDefaultValue(), user.getId());
		assertEquals("", user.getName());
		assertEquals("", user.getEmail());
		assertEquals(UserJpaEntity.STATUS_ACTIVE, user.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), user.getCreatedAt());
	}

	@Test
	@DisplayName("guarda los datos sin espacios en los extremos")
	void keepsTrimmedValues() {
		final UUID id = UUIDHelper.generateNew();
		final Instant createdAt = Instant.parse("2026-10-17T15:00:00Z");
		final UserJpaEntity user = new UserJpaEntity();

		user.setId(id);
		user.setName("  Lector de prueba  ");
		user.setEmail(" lector@bookhive.test ");
		user.setStatus(" " + UserJpaEntity.STATUS_SUSPENDED + " ");
		user.setCreatedAt(createdAt);

		assertEquals(id, user.getId());
		assertEquals("Lector de prueba", user.getName());
		assertEquals("lector@bookhive.test", user.getEmail());
		assertEquals(UserJpaEntity.STATUS_SUSPENDED, user.getStatus());
		assertEquals(createdAt, user.getCreatedAt());
	}

	@Test
	@DisplayName("convierte los nulos en valores por defecto")
	void replacesNulls() {
		final UserJpaEntity user = new UserJpaEntity();
		user.setStatus(UserJpaEntity.STATUS_RETIRED);

		user.setId(null);
		user.setName(null);
		user.setEmail(null);
		user.setStatus(null);
		user.setCreatedAt(null);

		assertEquals(UUIDHelper.getDefaultValue(), user.getId());
		assertEquals("", user.getName());
		assertEquals("", user.getEmail());
		assertEquals(UserJpaEntity.STATUS_ACTIVE, user.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), user.getCreatedAt());
	}

}
