package co.com.bookhive.crosscutting.exception;

import co.com.bookhive.crosscutting.helper.ObjectHelper;

public class BookHiveException extends RuntimeException {

	private static final long serialVersionUID = -693023700649543159L;

	public enum Kind {
		MISSING_DATA,
		STATE_DOES_NOT_ALLOW_IT,
		NOT_ALLOWED,
		UNAVAILABLE,
		UNEXPECTED
	}

	private final transient Throwable rootCause;
	private final String userMessageKey;
	private final String technicalMessage;
	private final Kind kind;

	private BookHiveException(
			final Throwable rootCause,
			final String userMessageKey,
			final String technicalMessage,
			final Kind kind) {

		super(technicalMessage);
		this.rootCause = ObjectHelper.getDefaultValue(rootCause, new Exception());
		this.userMessageKey = ObjectHelper.getDefaultValue(userMessageKey, "");
		this.technicalMessage = ObjectHelper.getDefaultValue(technicalMessage, "");
		this.kind = ObjectHelper.getDefaultValue(kind, Kind.UNEXPECTED);
	}

	public static BookHiveException create(final String userMessageKey) {
		return create(userMessageKey, userMessageKey);
	}

	public static BookHiveException create(final String userMessageKey, final String technicalMessage) {
		return new BookHiveException(new Exception(), userMessageKey, technicalMessage, Kind.MISSING_DATA);
	}

	public static BookHiveException createConflict(
			final String userMessageKey, final String technicalMessage) {

		return new BookHiveException(
				new Exception(), userMessageKey, technicalMessage, Kind.STATE_DOES_NOT_ALLOW_IT);
	}

	public static BookHiveException createNotAllowed(
			final String userMessageKey, final String technicalMessage) {

		return new BookHiveException(new Exception(), userMessageKey, technicalMessage, Kind.NOT_ALLOWED);
	}

	public static BookHiveException createUnexpected(
			final String userMessageKey, final String technicalMessage) {

		return new BookHiveException(new Exception(), userMessageKey, technicalMessage, Kind.UNEXPECTED);
	}

	public static BookHiveException createUnexpected(
			final Throwable rootCause,
			final String userMessageKey,
			final String technicalMessage) {

		return new BookHiveException(rootCause, userMessageKey, technicalMessage, Kind.UNEXPECTED);
	}

	public static BookHiveException createUnavailable(
			final Throwable rootCause,
			final String userMessageKey,
			final String technicalMessage) {

		return new BookHiveException(rootCause, userMessageKey, technicalMessage, Kind.UNAVAILABLE);
	}

	public Throwable getRootCause() {
		return rootCause;
	}

	public String getUserMessageKey() {
		return userMessageKey;
	}

	public String getTechnicalMessage() {
		return technicalMessage;
	}

	public Kind getKind() {
		return kind;
	}

}
