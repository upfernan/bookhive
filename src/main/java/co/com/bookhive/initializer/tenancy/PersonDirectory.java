package co.com.bookhive.initializer.tenancy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class PersonDirectory {

	private static final String ACTIVE_STATUS = "ACTIVA";

	private static final String FIND_ACTIVE_PERSON =
			"SELECT id, institucion_id FROM compartido.persona"
					+ " WHERE identidad = ? AND estado = ?";

	private final DataSource dataSource;

	public PersonDirectory(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public Person findActive(final String identity) {

		if (TextHelper.isDefaultValue(identity)) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_NOT_RESOLVED,
					"El pase no trae identificador de persona, y es lo único que se le pide"
							+ " al proveedor de identidad (ADR-028).");
		}

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(FIND_ACTIVE_PERSON)) {

			statement.setString(1, identity.trim());
			statement.setString(2, ACTIVE_STATUS);

			return readPerson(statement, identity.trim());

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.REQUEST_UNEXPECTED,
					"Falló la consulta del directorio de personas para " + identity + ".");
		}
	}

	private static Person readPerson(final PreparedStatement statement, final String identity)
			throws SQLException {

		try (ResultSet result = statement.executeQuery()) {

			if (!result.next()) {
				throw BookHiveException.createConflict(
						MessageKey.SESSION_NOT_RESOLVED,
						"La persona " + identity + " se autenticó pero no está registrada como"
								+ " ACTIVA en compartido.persona, así que no pertenece a ninguna"
								+ " institución de BookHive.");
			}

			return new Person(
					result.getObject("id", UUID.class),
					result.getObject("institucion_id", UUID.class));
		}
	}

	public record Person(UUID id, UUID institution) {

	}

}
