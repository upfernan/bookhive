package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.SchemaName;
import co.com.bookhive.access.services.output.ports.repositories.InstitutionRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.InstitutionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class InstitutionRepositoryAdapter implements InstitutionRepository {

	private static final String FIND_INSTITUTIONS = "SELECT id, esquema FROM compartido.institucion";

	private final DataSource dataSource;

	public InstitutionRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public List<InstitutionEntity> findByCriteria(final InstitutionCriteria criteria) {

		final SqlFilter filter = new SqlFilter(FIND_INSTITUTIONS);

		if (ObjectHelper.isNull(criteria)) {
			return List.of();
		}
		if (criteria.filtersById()) {
			filter.add("id = ?", criteria.getId());
		}
		if (criteria.filtersByStatus()) {
			filter.add("estado = ?", criteria.getStatus());
		}

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = filter.prepare(connection)) {

			return readInstitutions(statement);

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.REQUEST_UNEXPECTED,
					"Falló la consulta del directorio de instituciones para " + criteria.getId() + ".");
		}
	}

	private static List<InstitutionEntity> readInstitutions(final PreparedStatement statement)
			throws SQLException {

		final List<InstitutionEntity> institutions = new ArrayList<>();

		try (ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				final InstitutionEntity institution = new InstitutionEntity();
				institution.setId(result.getObject("id", UUID.class));
				institution.setSchema(SchemaName.requireValid(result.getString("esquema")));
				institutions.add(institution);
			}
		}

		return institutions;
	}

}
