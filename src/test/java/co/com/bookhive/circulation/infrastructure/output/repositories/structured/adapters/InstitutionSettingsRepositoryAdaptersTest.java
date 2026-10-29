package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.InstitutionParameterChangeJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.InstitutionParametersJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParameterChangeJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParametersJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParameterChangeEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;

class InstitutionSettingsRepositoryAdaptersTest {

	private static final UUID ID = UUID.randomUUID();

	@Test
	@DisplayName("guarda los parámetros cambiados de la institución")
	void updatesParameters() {
		final FakeJpaRepository<InstitutionParametersJpaEntity> repository = FakeJpaRepository.storing();
		final InstitutionParametersEntity parameters = new InstitutionParametersEntity();
		parameters.setId(ID);
		parameters.setLoanDays(20);

		new InstitutionParametersRepositoryAdapter(repository.as(InstitutionParametersJpaRepository.class),
				new FakeEntityMapper<>(jpa -> new InstitutionParametersEntity(), entity -> {
					final InstitutionParametersJpaEntity jpa = new InstitutionParametersJpaEntity();
					jpa.setId(entity.getId());
					jpa.setLoanDays(entity.getLoanDays());
					return jpa;
				})).update(parameters);

		assertEquals(20, repository.saved().get(0).getLoanDays());
	}

	@Test
	@DisplayName("guarda cada cambio de parámetro en el historial")
	void recordsChange() {
		final FakeJpaRepository<InstitutionParameterChangeJpaEntity> repository = FakeJpaRepository.storing();
		final InstitutionParameterChangeEntity change =
				new InstitutionParameterChangeEntity(ID, "daily_fine_rate", "1000.00", "2500.00", ID, null);

		new InstitutionParameterChangeRepositoryAdapter(repository.as(InstitutionParameterChangeJpaRepository.class),
				new FakeEntityMapper<>(InstitutionSettingsRepositoryAdaptersTest::toEntity,
						InstitutionSettingsRepositoryAdaptersTest::toJpaEntity)).create(change);

		final InstitutionParameterChangeJpaEntity saved = repository.saved().get(0);
		assertEquals("daily_fine_rate", saved.getParameter());
		assertEquals("1000.00", saved.getPreviousValue());
		assertEquals("2500.00", saved.getNewValue());
		assertEquals(ID, saved.getChangedBy());
		assertEquals(change, toEntity(saved));
	}

	private static InstitutionParameterChangeEntity toEntity(final InstitutionParameterChangeJpaEntity jpa) {
		return new InstitutionParameterChangeEntity(
				jpa.getId(), jpa.getParameter(), jpa.getPreviousValue(), jpa.getNewValue(), jpa.getChangedBy(), jpa.getChangedAt());
	}

	private static InstitutionParameterChangeJpaEntity toJpaEntity(final InstitutionParameterChangeEntity entity) {
		final InstitutionParameterChangeJpaEntity jpa = new InstitutionParameterChangeJpaEntity();
		jpa.setId(entity.id());
		jpa.setParameter(entity.parameter());
		jpa.setPreviousValue(entity.previousValue());
		jpa.setNewValue(entity.newValue());
		jpa.setChangedBy(entity.changedBy());
		jpa.setChangedAt(Instant.EPOCH);
		return jpa;
	}

}
