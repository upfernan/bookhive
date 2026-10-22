package co.com.bookhive.crosscutting.messages;

public final class MessageKey {

	public static final String LOAN_DATA_REQUIRED = "loan.data.required";
	public static final String LOAN_BOOK_COPY_REQUIRED = "loan.book-copy.required";
	public static final String LOAN_USER_REQUIRED = "loan.user.required";
	public static final String LOAN_LIBRARIAN_REQUIRED = "loan.librarian.required";
	public static final String LOAN_INSTITUTION_REQUIRED = "loan.institution.required";
	public static final String LOAN_DATE_REQUIRED = "loan.date.required";
	public static final String LOAN_DATE_IN_THE_FUTURE = "loan.date.in-the-future";
	public static final String LOAN_NOT_CONFIRMED = "loan.not-confirmed";
	public static final String LOAN_UNEXPECTED = "loan.unexpected";

	public static final String BOOK_COPY_NOT_FOUND = "book-copy.not-found";
	public static final String BOOK_COPY_NOT_AVAILABLE = "book-copy.not-available";
	public static final String BOOK_COPY_REFERENCE_ONLY = "book-copy.reference-only";

	public static final String USER_WITH_PENDING_FINES = "user.with-pending-fines";
	public static final String USER_AT_LOAN_LIMIT = "user.at-loan-limit";

	public static final String INSTITUTION_NOT_ACTIVE = "institution.not-active";
	public static final String INSTITUTION_WITHOUT_PARAMETERS = "institution.without-parameters";

	public static final String SESSION_NOT_RESOLVED = "session.not-resolved";
	public static final String SESSION_ID_TOKEN_REQUIRED = "session.id-token.required";
	public static final String SESSION_NOT_OPENED = "session.not-opened";
	public static final String SESSION_PROVIDER_UNAVAILABLE = "session.provider-unavailable";

	public static final String PERMISSION_DENIED = "permission.denied";

	public static final String SEARCH_TEXT_TOO_LONG = "search.text.too-long";
	public static final String SEARCH_PAGE_INVALID = "search.page.invalid";
	public static final String SEARCH_UNAVAILABLE = "search.unavailable";
	public static final String REQUEST_UNEXPECTED = "request.unexpected";
	public static final String REQUEST_INVALID_DATA = "request.invalid-data";
	public static final String REQUEST_UNHANDLED = "request.unhandled";
	public static final String REQUEST_BUSY = "request.busy";

	private MessageKey() {
		super();
	}

}
