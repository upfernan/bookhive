package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.SchemaName;
import co.com.bookhive.access.services.output.ports.repositories.PersonStatusRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class PersonStatusRepositoryAdapter implements PersonStatusRepository {

	private static final String OF_INSTITUTION =
			" WHERE p.id = ? AND p.institucion_id = (SELECT i.id FROM compartido.institucion i WHERE i.esquema = ?)";
	private static final String CHANGE_STATUS = "UPDATE compartido.persona p SET estado = ?" + OF_INSTITUTION;
	private static final String ANONYMIZE =
			"UPDATE compartido.persona p SET estado = 'INACTIVA', identidad = 'depurado-' || p.id::text" + OF_INSTITUTION;

	private final DataSource dataSource;

	public PersonStatusRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public void changeStatus(final UUID person, final String schema, final String status) {
		run(CHANGE_STATUS, person, schema, status);
	}

	@Override
	public void anonymize(final UUID person, final String schema) {
		run(ANONYMIZE, person, schema, null);
	}

	private void run(final String sql, final UUID person, final String schema, final String status) {
		final String validSchema = SchemaName.requireValid(schema);
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			int position = 1;
			if (status != null) {
				statement.setString(position++, status);
			}
			statement.setObject(position++, person);
			statement.setString(position, validSchema);
			statement.executeUpdate();
		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception, MessageKey.REQUEST_UNEXPECTED, "Falló el cambio de la persona " + person + " en " + validSchema + ".");
		}
	}

}
