package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.sql.SQLException;
import java.util.Locale;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LoanReturnJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanReturnJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanReturnRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class LoanReturnRepositoryAdapter implements LoanReturnRepository {

	static final String ONE_RETURN_PER_LOAN = "uq_loan_return_loan";
	private static final String UNIQUE_VIOLATION = "23505";

	private final LoanReturnJpaRepository repository;
	private final EntityMapper<LoanReturnEntity, LoanReturnJpaEntity> mapper;

	public LoanReturnRepositoryAdapter(
			final LoanReturnJpaRepository repository,
			final EntityMapper<LoanReturnEntity, LoanReturnJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void create(final LoanReturnEntity entity) {
		try {
			repository.saveAndFlush(mapper.toJpaEntity(entity));
		} catch (final RuntimeException exception) {
			if (isSecondReturn(exception)) {
				throw BookHiveException.createConflict(
						MessageKey.LOAN_RETURN_NOT_OPEN,
						"Dos devoluciones simultáneas del mismo préstamo: la base de datos rechazó la segunda (RT-01).");
			}
			throw exception;
		}
	}

	private static boolean isSecondReturn(final Throwable exception) {
		Throwable current = exception;
		while (!ObjectHelper.isNull(current)) {
			if (current instanceof ConstraintViolationException violation
					&& mentionsOneReturnPerLoan(violation.getConstraintName())) {
				return true;
			}
			if (current instanceof SQLException sql
					&& UNIQUE_VIOLATION.equals(sql.getSQLState())
					&& mentionsOneReturnPerLoan(sql.getMessage())) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static boolean mentionsOneReturnPerLoan(final String text) {
		return TextHelper.getDefaultValue(text).toLowerCase(Locale.ROOT).contains(ONE_RETURN_PER_LOAN);
	}

}
