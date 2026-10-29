package co.com.bookhive.circulation.services.usecase.institution.configureinstitution.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.institution.configureinstitution.InstitutionSettingsDomain;
import co.com.bookhive.circulation.domain.institution.configureinstitution.ParameterChange;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParameterChangeEntity;
import co.com.bookhive.circulation.services.usecase.institution.findinstitutionsettings.impl.FindInstitutionSettingsUseCaseImpl;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class ConfigureInstitutionUseCaseImplTest {

	private static final UUID INSTITUTION = UUID.randomUUID();
	private static final UUID ADMINISTRATOR = UUID.randomUUID();
	private static final String BOGOTA = "America/Bogota";

	private FakeRepositories.Parameters parameters;
	private List<InstitutionParameterChangeEntity> changes;
	private List<String> audited;

	@BeforeEach
	void prepare() {
		parameters = new FakeRepositories.Parameters().with(15, 3, new BigDecimal("1000.00"));
		changes = new ArrayList<>();
		audited = new ArrayList<>();
	}

	@Test
	@DisplayName("cambia la tarifa y deja registrado quién, el valor anterior y el nuevo")
	void changesRateAndRecordsHistory() {
		final InstitutionSettingsDomain result = useCase().execute(
				new InstitutionSettingsDomain(15, 3, new BigDecimal("2500"), BOGOTA));

		assertEquals(new BigDecimal("2500.00"), result.dailyFineRate());
		assertEquals(new BigDecimal("2500.00"), parameters.updated().get(0).getDailyFineRate());
		assertEquals(1, changes.size());
		final InstitutionParameterChangeEntity change = changes.get(0);
		assertEquals(ParameterChange.DAILY_FINE_RATE, change.parameter());
		assertEquals("1000.00", change.previousValue());
		assertEquals("2500.00", change.newValue());
		assertEquals(ADMINISTRATOR, change.changedBy());
		assertEquals(List.of(AuditPort.INSTITUTION_CONFIGURED + " daily_fine_rate 1000.00->2500.00"), audited);
	}

	@Test
	@DisplayName("si no cambia nada no guarda ni audita")
	void unchangedSettingsDoNothing() {
		final InstitutionSettingsDomain result = useCase().execute(
				new InstitutionSettingsDomain(15, 3, new BigDecimal("1000"), BOGOTA));

		assertEquals(15, result.loanDays());
		assertTrue(parameters.updated().isEmpty());
		assertTrue(changes.isEmpty());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("una regla inválida no cambia nada")
	void invalidSettingsChangeNothing() {
		final ConfigureInstitutionUseCaseImpl useCase = useCase();
		final InstitutionSettingsDomain invalid = new InstitutionSettingsDomain(0, 3, BigDecimal.ONE, BOGOTA);

		assertEquals(MessageKey.INSTITUTION_SETTINGS_LOAN_DAYS_INVALID,
				assertThrows(BookHiveException.class, () -> useCase.execute(invalid)).getUserMessageKey());
		assertEquals(MessageKey.INSTITUTION_SETTINGS_DATA_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getUserMessageKey());
		assertTrue(parameters.updated().isEmpty());
	}

	@Test
	@DisplayName("una institución sin su fila de parámetros es un error inesperado")
	void institutionWithoutParametersIsUnexpected() {
		final ConfigureInstitutionUseCaseImpl useCase = new ConfigureInstitutionUseCaseImpl(
				new FakeRepositories.Parameters(), changes::add, session(), (a, i, p, d) -> audited.add(a));
		final InstitutionSettingsDomain request = new InstitutionSettingsDomain(15, 3, BigDecimal.ONE, BOGOTA);
		final FindInstitutionSettingsUseCaseImpl find = new FindInstitutionSettingsUseCaseImpl(new FakeRepositories.Parameters());

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> useCase.execute(request)).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED, assertThrows(BookHiveException.class, find::execute).getKind());
	}

	@Test
	@DisplayName("consulta las reglas vigentes")
	void findsCurrentSettings() {
		final InstitutionSettingsDomain settings = new FindInstitutionSettingsUseCaseImpl(parameters).execute();

		assertEquals(new InstitutionSettingsDomain(15, 3, new BigDecimal("1000.00"), BOGOTA), settings);
	}

	@Test
	@DisplayName("no se crea sin sus dependencias")
	void requiresDependencies() {
		final SessionContextPort session = session();

		assertThrows(BookHiveException.class, () -> new ConfigureInstitutionUseCaseImpl(parameters, changes::add, session, null));
		assertThrows(BookHiveException.class, () -> new FindInstitutionSettingsUseCaseImpl(null));
	}

	private ConfigureInstitutionUseCaseImpl useCase() {
		return new ConfigureInstitutionUseCaseImpl(parameters, changes::add, session(),
				(action, institution, person, detail) -> audited.add(action + " " + detail));
	}

	private static SessionContextPort session() {
		return new SessionContextPort() {

			@Override
			public UUID getInstitution() {
				return INSTITUTION;
			}

			@Override
			public UUID getLibrarian() {
				return ADMINISTRATOR;
			}

		};
	}

}
