package co.com.bookhive.circulation.domain.institution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class CurrentInstitutionTest {

	private static final Clock EIGHT_PM_IN_BOGOTA = Clock.fixed(Instant.parse("2026-10-24T01:00:00Z"), ZoneOffset.UTC);

	@Test
	@DisplayName("a las 8 p. m. en Bogotá todavía es el mismo día, aunque en UTC ya sea el siguiente")
	void todayUsesTheInstitutionZone() {
		assertEquals(LocalDate.of(2026, 10, 23), CurrentInstitution.today(parametersIn("America/Bogota"), EIGHT_PM_IN_BOGOTA));
		assertEquals(LocalDate.of(2026, 10, 24), CurrentInstitution.today(parametersIn("UTC"), EIGHT_PM_IN_BOGOTA));
		assertEquals(LocalDate.of(2026, 10, 23), CurrentInstitution.today(parametersIn("America/Sao_Paulo"), EIGHT_PM_IN_BOGOTA));
	}

	@Test
	@DisplayName("una zona vacía o inválida, o sin parámetros, usa la zona de arranque")
	void invalidZoneFallsBackToDefault() {
		final ZoneId bogota = ZoneId.of(InstitutionParametersEntity.DEFAULT_TIME_ZONE);

		assertEquals(bogota, CurrentInstitution.zoneOf(parametersIn("Marte/Olympus")));
		assertEquals(bogota, CurrentInstitution.zoneOf(parametersIn("  ")));
		assertEquals(bogota, CurrentInstitution.zoneOf(null));
	}

	@Test
	@DisplayName("los parámetros salen del repositorio y sin repositorio es un error inesperado")
	void readsParametersFromTheRepository() {
		final InstitutionParametersEntity found = CurrentInstitution.parameters(
				new FakeRepositories.Parameters().with(15, 3, new BigDecimal("500"), "America/Lima"));

		assertEquals("America/Lima", found.getTimeZone());
		assertEquals(0, CurrentInstitution.parameters(new FakeRepositories.Parameters()).getLoanDays());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> CurrentInstitution.parameters(null)).getKind());
	}

	private static InstitutionParametersEntity parametersIn(final String zone) {
		final InstitutionParametersEntity parameters = new InstitutionParametersEntity();
		parameters.setTimeZone(zone);
		return parameters;
	}

}
