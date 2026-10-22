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

import co.com.bookhive.access.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PersonCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class PersonRepositoryAdapter implements PersonRepository {

	private static final String FIND_PEOPLE = "SELECT id, institucion_id FROM compartido.persona";

	private final DataSource dataSource;

	public PersonRepositoryAdapter(final DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public List<PersonEntity> findByCriteria(final PersonCriteria criteria) {

		final SqlFilter filter = new SqlFilter(FIND_PEOPLE);

		if (ObjectHelper.isNull(criteria)) {
			return List.of();
		}
		if (criteria.filtersByIdentity()) {
			filter.add("identidad = ?", criteria.getIdentity());
		}
		if (criteria.filtersByStatus()) {
			filter.add("estado = ?", criteria.getStatus());
		}

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(filter.query())) {

			filter.bind(statement);
			return readPeople(statement);

		} catch (final SQLException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.REQUEST_UNEXPECTED,
					"Falló la consulta del directorio de personas para " + criteria.getIdentity() + ".");
		}
	}

	private static List<PersonEntity> readPeople(final PreparedStatement statement) throws SQLException {

		final List<PersonEntity> people = new ArrayList<>();

		try (ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				final PersonEntity person = new PersonEntity();
				person.setId(result.getObject("id", UUID.class));
				person.setInstitution(result.getObject("institucion_id", UUID.class));
				people.add(person);
			}
		}

		return people;
	}

}
