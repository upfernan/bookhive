package co.com.bookhive.circulation.domain.institution;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

public final class CurrentInstitution {

	private CurrentInstitution() {
		super();
	}

	public static InstitutionParametersEntity parameters(final InstitutionParametersRepository repository) {
		if (ObjectHelper.isNull(repository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"Se pidieron los parámetros de la institución sin su repositorio.");
		}
		return repository.findAll(PageCriteria.of(0, 1)).stream()
				.findFirst()
				.orElseGet(InstitutionParametersEntity::new);
	}

	public static ZoneId zoneOf(final InstitutionParametersEntity parameters) {
		final String zone = Optional.ofNullable(parameters)
				.map(InstitutionParametersEntity::getTimeZone)
				.orElse(InstitutionParametersEntity.DEFAULT_TIME_ZONE);
		try {
			return ZoneId.of(zone);
		} catch (final DateTimeException exception) {
			return ZoneId.of(InstitutionParametersEntity.DEFAULT_TIME_ZONE);
		}
	}

	public static LocalDate today(final InstitutionParametersEntity parameters) {
		return DateHelper.todayIn(zoneOf(parameters));
	}

	public static LocalDate today(final InstitutionParametersEntity parameters, final Clock clock) {
		return DateHelper.todayIn(zoneOf(parameters), clock);
	}

}
