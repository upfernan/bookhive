package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy.SchemaName;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class InstitutionRepositoryAdapter implements InstitutionRepository {

	private static final String ACTIVE_STATUS = "ACTIVA";

	private static final String FIND_ACTIVE_SCHEMA =
			"SELECT esquema FROM compartido.institucion WHERE id = ? AND estado = ?";

	private final DataSource dataSource;

	public InstitutionRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public String findActiveSchema(final UUID institution) {

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(FIND_ACTIVE_SCHEMA)) {

			statement.setObject(1, institution);
			statement.setString(2, ACTIVE_STATUS);

			return readSchema(statement, institution);

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.REQUEST_UNEXPECTED,
					"Falló la consulta del directorio de instituciones para " + institution + ".");
		}
	}

	private static String readSchema(final PreparedStatement statement, final UUID institution)
			throws SQLException {

		try (ResultSet result = statement.executeQuery()) {

			if (!result.next()) {
				throw BookHiveException.createConflict(
						MessageKey.INSTITUTION_NOT_ACTIVE,
						"No hay fila con estado " + ACTIVE_STATUS + " en compartido.institucion"
								+ " para la institución " + institution + ".");
			}

			return SchemaName.requireValid(result.getString("esquema"));
		}
	}

}
