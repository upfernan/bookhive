package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.FineSettlementJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineSettlementJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.FineSettlementRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineSettlementCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineSettlementEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class FineSettlementRepositoryAdapter implements FineSettlementRepository {

	private final FineSettlementJpaRepository repository;
	private final EntityMapper<FineSettlementEntity, FineSettlementJpaEntity> mapper;

	public FineSettlementRepositoryAdapter(
			final FineSettlementJpaRepository repository,
			final EntityMapper<FineSettlementEntity, FineSettlementJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public boolean settle(final FineSettlementEntity settlement) {
		if (ObjectHelper.isNull(settlement)
				|| UUIDHelper.isDefaultValue(settlement.id())
				|| UUIDHelper.isDefaultValue(settlement.fine())
				|| TextHelper.isDefaultValue(settlement.paymentReference())) {
			throw BookHiveException.createUnexpected(
					MessageKey.FINE_UNEXPECTED,
					"Se intentó saldar una multa sin identificador, multa o referencia de pago.");
		}
		return repository.settle(
				settlement.id(),
				settlement.fine(),
				settlement.paymentReference(),
				settlement.amount()) == 1;
	}

	@Override
	public List<FineSettlementEntity> findByCriteria(final FineSettlementCriteria criteria) {
		final FineSettlementCriteria safeCriteria = ObjectHelper.getDefaultValue(
				criteria, FineSettlementCriteria.ofFine(UUIDHelper.getDefaultValue()));
		final Specification<FineSettlementJpaEntity> specification = (root, query, builder) ->
				builder.equal(root.get("fine"), safeCriteria.getFine());
		return repository.findAll(specification, Sort.by("settledAt"))
				.stream().map(mapper::toEntity).toList();
	}

}
