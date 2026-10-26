package co.com.bookhive.crosscutting.messages;

public final class MessageKey {

	public static final String LOAN_DATA_REQUIRED = "loan.data.required";
	public static final String LOAN_BOOK_COPY_REQUIRED = "loan.book-copy.required";
	public static final String LOAN_USER_REQUIRED = "loan.user.required";
	public static final String LOAN_USER_NOT_FOUND = "loan.user.not-found";
	public static final String LOAN_LIBRARIAN_REQUIRED = "loan.librarian.required";
	public static final String LOAN_INSTITUTION_REQUIRED = "loan.institution.required";
	public static final String LOAN_DATE_REQUIRED = "loan.date.required";
	public static final String LOAN_DATE_IN_THE_FUTURE = "loan.date.in-the-future";
	public static final String LOAN_NOT_CONFIRMED = "loan.not-confirmed";
	public static final String LOAN_UNEXPECTED = "loan.unexpected";
	public static final String LOAN_RETURN_BOOK_COPY_REQUIRED = "loan.return.book-copy.required";
	public static final String LOAN_RETURN_NOT_OPEN = "loan.return.not-open";
	public static final String LOAN_RETURN_NOT_CONFIRMED = "loan.return.not-confirmed";

	public static final String BOOK_COPY_NOT_FOUND = "book-copy.not-found";
	public static final String BOOK_COPY_LOCATION_INVALID = "book-copy.location.invalid";
	public static final String BOOK_COPY_WITHDRAWN = "book-copy.withdrawn";
	public static final String BOOK_COPY_NOT_WITHDRAWABLE = "book-copy.not-withdrawable";
	public static final String BRANCH_NAME_INVALID = "branch.name.invalid";
	public static final String BRANCH_NAME_TAKEN = "branch.name.taken";
	public static final String BRANCH_NOT_FOUND = "branch.not-found";
	public static final String LOCATION_LEVELS_INVALID = "location.levels.invalid";
	public static final String LOCATION_LEVELS_IN_USE = "location.levels.in-use";
	public static final String BOOK_COPY_NOT_AVAILABLE = "book-copy.not-available";
	public static final String BOOK_COPY_REFERENCE_ONLY = "book-copy.reference-only";
	public static final String BOOK_COPY_BARCODE_REQUIRED = "book-copy.barcode.required";
	public static final String BOOK_COPY_RESERVED = "book-copy.reserved";

	public static final String USER_WITH_PENDING_FINES = "user.with-pending-fines";
	public static final String USER_AT_LOAN_LIMIT = "user.at-loan-limit";
	public static final String USER_EMAIL_REQUIRED = "user.email.required";
	public static final String USER_NOT_FOUND = "user.not-found";
	public static final String USER_SUSPENDED = "user.suspended";
	public static final String USER_RETIRED = "user.retired";

	public static final String RESERVATION_BOOK_REQUIRED = "reservation.book.required";
	public static final String RESERVATION_USER_NOT_FOUND = "reservation.user.not-found";
	public static final String RESERVATION_BOOK_NOT_RESERVABLE = "reservation.book.not-reservable";
	public static final String RESERVATION_COPIES_AVAILABLE = "reservation.copies-available";
	public static final String RESERVATION_ALREADY_ACTIVE = "reservation.already-active";
	public static final String RESERVATION_ID_REQUIRED = "reservation.id.required";
	public static final String RESERVATION_NOT_FOUND = "reservation.not-found";
	public static final String RESERVATION_NOT_CANCELLABLE = "reservation.not-cancellable";

	public static final String FINE_ID_REQUIRED = "fine.id.required";
	public static final String FINE_NOT_FOUND = "fine.not-found";
	public static final String FINE_ALREADY_PAID = "fine.already-paid";
	public static final String FINE_PAYMENT_DATA_REQUIRED = "fine.payment.data.required";
	public static final String FINE_UNEXPECTED = "fine.unexpected";

	public static final String PAYMENT_REFERENCE_REQUIRED = "payment.reference.required";
	public static final String PAYMENT_TRANSACTION_REQUIRED = "payment.transaction.required";
	public static final String PAYMENT_NOT_FOUND = "payment.not-found";
	public static final String PAYMENT_AMOUNT_INVALID = "payment.amount.invalid";
	public static final String PAYMENT_TRANSACTION_MISMATCH = "payment.transaction.mismatch";
	public static final String PAYMENT_EVENT_NOT_AUTHENTIC = "payment.event.not-authentic";
	public static final String PAYMENT_GATEWAY_UNAVAILABLE = "payment.gateway.unavailable";
	public static final String PAYMENT_UNEXPECTED = "payment.unexpected";

	public static final String INSTITUTION_NOT_ACTIVE = "institution.not-active";
	public static final String INSTITUTION_WITHOUT_PARAMETERS = "institution.without-parameters";
	public static final String INSTITUTION_SETTINGS_DATA_REQUIRED = "institution.settings.data.required";
	public static final String INSTITUTION_SETTINGS_LOAN_DAYS_INVALID = "institution.settings.loan-days.invalid";
	public static final String INSTITUTION_SETTINGS_MAX_LOANS_INVALID = "institution.settings.max-loans.invalid";
	public static final String INSTITUTION_SETTINGS_FINE_RATE_INVALID = "institution.settings.fine-rate.invalid";
	public static final String INSTITUTION_SETTINGS_TIME_ZONE_INVALID = "institution.settings.time-zone.invalid";

	public static final String SESSION_NOT_RESOLVED = "session.not-resolved";
	public static final String SESSION_ID_TOKEN_REQUIRED = "session.id-token.required";
	public static final String SESSION_NOT_OPENED = "session.not-opened";
	public static final String SESSION_PROVIDER_UNAVAILABLE = "session.provider-unavailable";

	public static final String PERMISSION_DENIED = "permission.denied";
	public static final String DATA_POLICY_NOT_ACCEPTED = "data-policy.not-accepted";
	public static final String DATA_POLICY_VERSION_REQUIRED = "data-policy.version.required";
	public static final String DATA_POLICY_VERSION_OUTDATED = "data-policy.version.outdated";
	public static final String DATA_POLICY_NOT_PUBLISHED = "data-policy.not-published";

	public static final String SEARCH_TEXT_TOO_LONG = "search.text.too-long";
	public static final String SEARCH_PAGE_INVALID = "search.page.invalid";
	public static final String SEARCH_UNAVAILABLE = "search.unavailable";
	public static final String MESSAGING_UNAVAILABLE = "messaging.unavailable";
	public static final String EMAIL_UNAVAILABLE = "email.unavailable";
	public static final String NOTIFICATION_DATA_INVALID = "notification.data.invalid";
	public static final String EMAIL_LOAN_REGISTERED_SUBJECT = "email.loan-registered.subject";
	public static final String EMAIL_LOAN_REGISTERED_BODY = "email.loan-registered.body";
	public static final String EMAIL_LOAN_DUE_SOON_SUBJECT = "email.loan-due-soon.subject";
	public static final String EMAIL_LOAN_DUE_SOON_BODY = "email.loan-due-soon.body";
	public static final String EMAIL_FINE_GENERATED_SUBJECT = "email.fine-generated.subject";
	public static final String EMAIL_FINE_GENERATED_BODY = "email.fine-generated.body";
	public static final String EMAIL_FINE_PAID_SUBJECT = "email.fine-paid.subject";
	public static final String EMAIL_FINE_PAID_BODY = "email.fine-paid.body";
	public static final String EMAIL_RESERVATION_READY_SUBJECT = "email.reservation-ready.subject";
	public static final String EMAIL_RESERVATION_READY_BODY = "email.reservation-ready.body";
	public static final String REQUEST_UNEXPECTED = "request.unexpected";
	public static final String REQUEST_INVALID_DATA = "request.invalid-data";
	public static final String REQUEST_UNHANDLED = "request.unhandled";
	public static final String REQUEST_BUSY = "request.busy";

	private MessageKey() {
		super();
	}

}
