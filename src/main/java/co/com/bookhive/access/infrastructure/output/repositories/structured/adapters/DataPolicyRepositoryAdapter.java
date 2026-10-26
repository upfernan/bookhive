package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantStatements;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyRepository;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

@Component
public class DataPolicyRepositoryAdapter implements DataPolicyRepository {

	private static final String FIND_CURRENT =
			"SELECT version, content, published_at FROM data_policy ORDER BY version DESC LIMIT 1";

	private final DataSource dataSource;

	public DataPolicyRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public Optional<DataPolicyEntity> findCurrent() {

		return TenantStatements.run(dataSource, FIND_CURRENT, "Falló la consulta de la política de datos vigente.",
				statement -> {
					try (ResultSet result = statement.executeQuery()) {
						if (!result.next()) {
							return Optional.empty();
						}
						final OffsetDateTime publishedAt = result.getObject("published_at", OffsetDateTime.class);
						return Optional.of(new DataPolicyEntity(
								result.getObject("version", Integer.class),
								result.getString("content"),
								ObjectHelper.isNull(publishedAt) ? null : publishedAt.toInstant()));
					}
				});
	}

}
