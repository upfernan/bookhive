package co.com.bookhive.crosscutting.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateHelperTest {

	private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

	@Test
	@DisplayName("a las 7 p.m. de Bogotá en UTC ya es el día siguiente")
	void usesUtcDate() {
		final Instant sevenPm = ZonedDateTime.of(2026, 10, 17, 19, 0, 0, 0, BOGOTA).toInstant();

		assertEquals(LocalDate.of(2026, 10, 18), DateHelper.todayInUtc(Clock.fixed(sevenPm, BOGOTA)));
	}

	@Test
	@DisplayName("si el reloj es nulo usa el reloj UTC")
	void usesUtcClockWhenNull() {
		assertEquals(DateHelper.todayInUtc(), DateHelper.todayInUtc(null));
	}

	@Test
	@DisplayName("reemplaza las fechas nulas por el valor por defecto")
	void replacesNullDates() {
		assertEquals(DateHelper.getDefaultValue(), DateHelper.getDefaultValue((LocalDate) null));
		assertEquals(DateHelper.getDefaultTimestamp(), DateHelper.getDefaultValue((Instant) null));
		assertTrue(DateHelper.isDefaultValue((LocalDate) null));
		assertTrue(DateHelper.isDefaultValue((Instant) null));
		assertFalse(DateHelper.isDefaultValue(LocalDate.of(2026, 10, 17)));
	}

	@Test
	@DisplayName("calcula el día de hoy en la zona pedida, y sin zona o sin reloj usa UTC y la hora actual")
	void usesRequestedZone() {
		final Clock eightPmInBogota = Clock.fixed(Instant.parse("2026-10-24T01:00:00Z"), ZoneId.of("UTC"));

		assertEquals(LocalDate.of(2026, 10, 23), DateHelper.todayIn(ZoneId.of("America/Bogota"), eightPmInBogota));
		assertEquals(LocalDate.of(2026, 10, 24), DateHelper.todayIn(null, eightPmInBogota));
		assertEquals(LocalDate.now(ZoneId.of("America/Bogota")), DateHelper.todayIn(ZoneId.of("America/Bogota"), null));
		assertEquals(LocalDate.now(ZoneId.of("UTC")), DateHelper.todayIn(null));
		assertEquals(LocalDate.now(ZoneId.of("Asia/Tokyo")), DateHelper.todayIn(ZoneId.of("Asia/Tokyo")));
	}

}
