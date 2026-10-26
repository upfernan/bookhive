package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantStatements;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyAcceptanceRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.DataPolicyAcceptanceCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyAcceptanceEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

@Component
public class DataPolicyAcceptanceRepositoryAdapter implements DataPolicyAcceptanceRepository {

	private static final String INSERT =
			"INSERT INTO data_policy_acceptance (person_id, policy_version) VALUES (?, ?)"
					+ " ON CONFLICT (person_id, policy_version) DO NOTHING";
	private static final String FIND =
			"SELECT person_id, policy_version, accepted_at FROM data_policy_acceptance WHERE person_id = ?";
	private static final String OF_VERSION = " AND policy_version = ?";
	private static final String OF_CURRENT_POLICY = " AND policy_version = (SELECT max(version) FROM data_policy)";

	private final DataSource dataSource;

	public DataPolicyAcceptanceRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public void create(final DataPolicyAcceptanceEntity entity) {

		TenantStatements.run(dataSource, INSERT,
				"Falló el registro de la aceptación de la política de datos de " + entity.person() + ".",
				statement -> {
					statement.setObject(1, entity.person());
					statement.setInt(2, entity.version());
					return statement.executeUpdate();
				});
	}

	@Override
	public List<DataPolicyAcceptanceEntity> findByCriteria(final DataPolicyAcceptanceCriteria criteria) {

		if (ObjectHelper.isNull(criteria)) {
			return List.of();
		}

		final String sql = FIND + (criteria.meansCurrentPolicy() ? OF_CURRENT_POLICY : OF_VERSION);

		return TenantStatements.run(dataSource, sql,
				"Falló la consulta de la aceptación de la política de datos de " + criteria.person() + ".",
				statement -> {
					statement.setObject(1, criteria.person());
					if (!criteria.meansCurrentPolicy()) {
						statement.setInt(2, criteria.version());
					}
					return read(statement);
				});
	}

	private static List<DataPolicyAcceptanceEntity> read(final PreparedStatement statement) throws SQLException {

		final List<DataPolicyAcceptanceEntity> acceptances = new ArrayList<>();

		try (ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				final OffsetDateTime acceptedAt = result.getObject("accepted_at", OffsetDateTime.class);
				acceptances.add(new DataPolicyAcceptanceEntity(
						result.getObject("person_id", UUID.class),
						result.getObject("policy_version", Integer.class),
						ObjectHelper.isNull(acceptedAt) ? null : acceptedAt.toInstant()));
			}
		}

		return acceptances;
	}

}
