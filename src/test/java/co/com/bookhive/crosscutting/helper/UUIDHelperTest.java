package co.com.bookhive.crosscutting.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UUIDHelperTest {

	private static final Instant MOMENT = Instant.parse("2026-10-17T15:00:00Z");

	@Test
	@DisplayName("genera UUID versión 7 con la variante del estándar")
	void generatesVersionSeven() {
		final UUID uuid = UUIDHelper.generateNew();

		assertEquals(7, uuid.version());
		assertEquals(2, uuid.variant());
	}

	@Test
	@DisplayName("guarda la hora UTC en milisegundos en los primeros 48 bits")
	void storesTimestamp() {
		final UUID uuid = UUIDHelper.generateNew(Clock.fixed(MOMENT, ZoneOffset.UTC));

		assertEquals(MOMENT.toEpochMilli(), uuid.getMostSignificantBits() >>> 16);
	}

	@Test
	@DisplayName("los identificadores quedan ordenados por tiempo")
	void ordersByTime() {
		final UUID before = UUIDHelper.generateNew(Clock.fixed(MOMENT, ZoneOffset.UTC));
		final UUID after = UUIDHelper.generateNew(Clock.fixed(MOMENT.plusMillis(1), ZoneOffset.UTC));

		assertTrue(before.toString().compareTo(after.toString()) < 0);
	}

	@Test
	@DisplayName("no repite identificadores en el mismo milisegundo")
	void generatesUniqueValues() {
		final Clock clock = Clock.fixed(MOMENT, ZoneOffset.UTC);
		final Set<UUID> generated = new HashSet<>();

		for (int i = 0; i < 10_000; i++) {
			generated.add(UUIDHelper.generateNew(clock));
		}

		assertEquals(10_000, generated.size());
	}

	@Test
	@DisplayName("si el reloj es nulo usa el reloj UTC")
	void usesUtcClockWhenNull() {
		final UUID uuid = UUIDHelper.generateNew(null);

		assertEquals(7, uuid.version());
		assertFalse(UUIDHelper.isDefaultValue(uuid));
	}

	@Test
	@DisplayName("reemplaza los nulos por el valor por defecto")
	void replacesNullWithDefault() {
		assertEquals(UUIDHelper.getDefaultValue(), UUIDHelper.getDefaultValue(null));
		assertTrue(UUIDHelper.isDefaultValue(null));
		assertTrue(UUIDHelper.isDefaultValue(UUIDHelper.getDefaultValue()));
	}

}
