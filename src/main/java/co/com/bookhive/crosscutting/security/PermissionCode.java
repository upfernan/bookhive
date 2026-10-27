package co.com.bookhive.crosscutting.security;

public final class PermissionCode {

	public static final String LEND_BOOK = "loan.lend";
	public static final String RETURN_BOOK = "loan.return";
	public static final String SEARCH_CATALOG = "catalog.search";
	public static final String SYNC_CATALOG = "catalog.sync";
	public static final String RESERVE_BOOK = "reservation.create";
	public static final String PAY_FINE = "fine.pay";
	public static final String CONFIGURE_INSTITUTION = "institution.configure";

	public static final String VIEW_ACCOUNT = "account.view";

	public static final String LOCATE_BOOK_COPY = "book-copy.locate";
	public static final String RELOCATE_BOOK_COPY = "book-copy.relocate";
	public static final String WITHDRAW_BOOK_COPY = "book-copy.withdraw";

	public static final String MANAGE_CATALOG = "catalog.manage";
	public static final String MANAGE_CATEGORIES = "category.manage";

	public static final String MANAGE_USERS = "user.manage";

	private PermissionCode() {
		super();
	}

}
