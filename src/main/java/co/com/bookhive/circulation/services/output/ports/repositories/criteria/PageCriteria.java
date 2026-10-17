package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

public final class PageCriteria {

	private static final int FIRST_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;

	private final int page;
	private final int size;

	private PageCriteria(final int page, final int size) {
		super();
		this.page = Math.max(page, FIRST_PAGE);
		this.size = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
	}

	public static PageCriteria of(final int page, final int size) {
		return new PageCriteria(page, size);
	}

	public static PageCriteria defaultPage() {
		return new PageCriteria(FIRST_PAGE, DEFAULT_SIZE);
	}

	public int getPage() {
		return page;
	}

	public int getSize() {
		return size;
	}

}
