package co.com.bookhive.circulation.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class BorrowerChecks {

	private static final String THE_USER = "El usuario ";

	private BorrowerChecks() {
		super();
	}

	public static UserEntity requireExisting(final Optional<UserEntity> found, final UUID user, final String notFoundKey) {
		return found.filter(entity -> !UUIDHelper.isDefaultValue(entity.getId()))
				.orElseThrow(() -> BookHiveException.createConflict(
						notFoundKey,
						"No se encontró el usuario " + user + " en la institución en curso."));
	}

	public static void requireActive(final UserEntity user) {
		final String status = user.getStatus();

		if (UserEntity.STATUS_RETIRED.equals(status)) {
			throw BookHiveException.createConflict(
					MessageKey.USER_RETIRED,
					THE_USER + user.getId() + " está en estado " + UserEntity.STATUS_RETIRED + ".");
		}

		if (!UserEntity.STATUS_ACTIVE.equals(status)) {
			throw BookHiveException.createConflict(
					MessageKey.USER_SUSPENDED,
					THE_USER + user.getId() + " está en estado " + status
							+ " y para llevar ejemplares debe estar " + UserEntity.STATUS_ACTIVE + ".");
		}
	}

	public static void requireWithoutPendingFines(final FineRepository fines, final UUID user) {
		final List<FineEntity> pending = fines.findByCriteria(FineCriteria.pendingByUser(user));

		if (!pending.isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.USER_WITH_PENDING_FINES,
					THE_USER + user + " tiene " + pending.size()
							+ " multa(s) en estado " + FineEntity.STATUS_PENDING + ".");
		}
	}

}
