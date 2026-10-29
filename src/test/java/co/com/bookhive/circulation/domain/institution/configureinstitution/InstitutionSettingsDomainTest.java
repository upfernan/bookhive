package co.com.bookhive.circulation.domain.institution.configureinstitution;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class InstitutionSettingsDomainTest {

	private static final String BOGOTA = "America/Bogota";

	@Test
	@DisplayName("acepta reglas dentro de los límites")
	void acceptsValidSettings() {
		assertDoesNotThrow(new InstitutionSettingsDomain(15, 3, new BigDecimal("1500.50"), BOGOTA)::validate);
		assertDoesNotThrow(new InstitutionSettingsDomain(90, 20, BigDecimal.ZERO, "Europe/Madrid")::validate);
	}

	@Test
	@DisplayName("rechaza cada regla fuera de su límite con su propio mensaje")
	void rejectsEachInvalidRule() {
		assertEquals(MessageKey.INSTITUTION_SETTINGS_LOAN_DAYS_INVALID, keyOf(new InstitutionSettingsDomain(0, 3, BigDecimal.ONE, BOGOTA)));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_LOAN_DAYS_INVALID, keyOf(new InstitutionSettingsDomain(91, 3, BigDecimal.ONE, BOGOTA)));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_MAX_LOANS_INVALID, keyOf(new InstitutionSettingsDomain(15, 0, BigDecimal.ONE, BOGOTA)));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_MAX_LOANS_INVALID, keyOf(new InstitutionSettingsDomain(15, 21, BigDecimal.ONE, BOGOTA)));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_FINE_RATE_INVALID,
				keyOf(new InstitutionSettingsDomain(15, 3, new BigDecimal("-1"), BOGOTA)));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_FINE_RATE_INVALID,
				keyOf(new InstitutionSettingsDomain(15, 3, new BigDecimal("1000000.01"), BOGOTA)));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_FINE_RATE_INVALID,
				keyOf(new InstitutionSettingsDomain(15, 3, new BigDecimal("10.005"), BOGOTA)));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_TIME_ZONE_INVALID, keyOf(new InstitutionSettingsDomain(15, 3, BigDecimal.ONE, "Marte/Olympus")));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_TIME_ZONE_INVALID, keyOf(new InstitutionSettingsDomain(15, 3, BigDecimal.ONE, "UTC")));
		assertEquals(MessageKey.INSTITUTION_SETTINGS_TIME_ZONE_INVALID, keyOf(new InstitutionSettingsDomain(15, 3, null, null)));
	}

	@Test
	@DisplayName("solo cuenta como cambio lo que de verdad cambió, con el valor anterior y el nuevo")
	void detectsOnlyRealChanges() {
		final InstitutionSettingsDomain current = new InstitutionSettingsDomain(15, 3, new BigDecimal("1000.00"), BOGOTA);

		final List<ParameterChange> none = ParameterChange.between(current, new InstitutionSettingsDomain(15, 3, new BigDecimal("1000"), BOGOTA));
		final List<ParameterChange> some = ParameterChange.between(current, new InstitutionSettingsDomain(20, 3, new BigDecimal("1500"), BOGOTA));

		assertTrue(none.isEmpty());
		assertEquals(List.of(
				new ParameterChange(ParameterChange.LOAN_DAYS, "15", "20"),
				new ParameterChange(ParameterChange.DAILY_FINE_RATE, "1000.00", "1500.00")), some);
		assertEquals("loan_days 15->20", some.get(0).describe());
		assertEquals("", new ParameterChange(null, null, null).parameter());
	}

	private static String keyOf(final InstitutionSettingsDomain settings) {
		return assertThrows(BookHiveException.class, settings::validate).getUserMessageKey();
	}

}
