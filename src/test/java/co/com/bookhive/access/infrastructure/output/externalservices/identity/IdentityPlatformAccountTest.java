package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IdentityPlatformAccountTest {

	@Test
	@DisplayName("acepta sesiones abiertas desde que la cuenta las volvió a permitir")
	void acceptsSessionsAfterValidSince() {
		final IdentityPlatformAccount account = new IdentityPlatformAccount(100L, false);

		assertTrue(account.acceptsSessionStartedAt(100L));
		assertTrue(account.acceptsSessionStartedAt(150L));
		assertFalse(account.acceptsSessionStartedAt(99L));
	}

	@Test
	@DisplayName("una cuenta deshabilitada no acepta ninguna sesión")
	void disabledAccountAcceptsNothing() {
		assertFalse(new IdentityPlatformAccount(0L, true).acceptsSessionStartedAt(500L));
	}

}
