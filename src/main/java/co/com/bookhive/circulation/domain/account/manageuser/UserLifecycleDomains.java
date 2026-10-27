package co.com.bookhive.circulation.domain.account.manageuser;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class UserLifecycleDomains {

	public static final String ACTIVE = "ACTIVO";
	public static final String RETIRED = "RETIRADO";
	public static final String PURGED = "DEPURADO";
	public static final int MIN_RETENTION = 1;
	public static final int MAX_RETENTION = 120;

	private UserLifecycleDomains() {
		super();
	}

	public enum Change {
		DEACTIVATE,
		REACTIVATE,
		PURGE
	}

	public record Request(UUID user, Change change) {

		public Request {
			user = UUIDHelper.getDefaultValue(user);
			change = change == null ? Change.DEACTIVATE : change;
		}

		public void validate() {
			if (UUIDHelper.isDefaultValue(user)) {
				throw BookHiveException.create(MessageKey.USER_NOT_FOUND, "No llegó el usuario.");
			}
		}

	}

	public record Account(UUID user, String status, Instant deactivatedAt) {

		public Account {
			user = UUIDHelper.getDefaultValue(user);
			status = TextHelper.getDefaultValue(status);
		}

		public boolean isPurged() {
			return PURGED.equals(status);
		}

	}

	public record Retention(int months) {

		public void validate() {
			if (months < MIN_RETENTION || months > MAX_RETENTION) {
				throw BookHiveException.create(MessageKey.USER_RETENTION_INVALID, "Se pidieron " + months + " meses de conservación.");
			}
		}

	}

	public record PurgeRun(String institution, int purged, int skipped) {
	}

}
