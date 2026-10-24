package co.com.bookhive.crosscutting.security;

public final class PermissionCode {

	public static final String LEND_BOOK = "loan.lend";
	public static final String RETURN_BOOK = "loan.return";
	public static final String SEARCH_CATALOG = "catalog.search";
	public static final String SYNC_CATALOG = "catalog.sync";
	public static final String RESERVE_BOOK = "reservation.create";

	private PermissionCode() {
		super();
	}

}
