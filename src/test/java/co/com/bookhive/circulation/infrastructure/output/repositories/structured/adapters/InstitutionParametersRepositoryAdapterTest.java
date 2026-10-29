package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.InstitutionParametersJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParametersJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class InstitutionParametersRepositoryAdapterTest {

	private static final int LOAN_DAYS = 15;

	@Test
	@DisplayName("busca los parámetros por su identificador")
	void findsParametersById() {
		final InstitutionParametersRepositoryAdapter adapter = adapter(FakeJpaRepository.storing(jpaParameters()));

		assertEquals(Optional.of(LOAN_DAYS),
				adapter.findById(UUIDHelper.generateNew()).map(InstitutionParametersEntity::getLoanDays));
	}

	@Test
	@DisplayName("sin página consulta la primera con 20 elementos")
	void findsDefaultPage() {
		final FakeJpaRepository<InstitutionParametersJpaEntity> repository = FakeJpaRepository.storing(jpaParameters());

		final List<InstitutionParametersEntity> parameters = adapter(repository).findAll(null);

		assertEquals(1, parameters.size());
		assertEquals(0, repository.pages().get(0).getPageNumber());
		assertEquals(20, repository.pages().get(0).getPageSize());
	}

	private static InstitutionParametersRepositoryAdapter adapter(
			final FakeJpaRepository<InstitutionParametersJpaEntity> repository) {
		return new InstitutionParametersRepositoryAdapter(repository.as(InstitutionParametersJpaRepository.class),
				new FakeEntityMapper<>(InstitutionParametersRepositoryAdapterTest::toEntity,
						InstitutionParametersRepositoryAdapterTest::toJpaEntity));
	}

	private static InstitutionParametersJpaEntity jpaParameters() {
		final InstitutionParametersJpaEntity jpaEntity = new InstitutionParametersJpaEntity();
		jpaEntity.setLoanDays(LOAN_DAYS);
		return jpaEntity;
	}

	private static InstitutionParametersEntity toEntity(final InstitutionParametersJpaEntity jpaEntity) {
		final InstitutionParametersEntity entity = new InstitutionParametersEntity();
		entity.setLoanDays(jpaEntity.getLoanDays());
		return entity;
	}

	private static InstitutionParametersJpaEntity toJpaEntity(final InstitutionParametersEntity entity) {
		final InstitutionParametersJpaEntity jpaEntity = new InstitutionParametersJpaEntity();
		jpaEntity.setLoanDays(entity.getLoanDays());
		return jpaEntity;
	}

}
