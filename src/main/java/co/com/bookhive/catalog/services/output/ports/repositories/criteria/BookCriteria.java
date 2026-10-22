package co.com.bookhive.catalog.services.output.ports.repositories.criteria;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

public final class BookCriteria {

	private final List<UUID> ids;
	private final String status;
	private final PageCriteria page;

	private BookCriteria(final List<UUID> ids, final String status, final PageCriteria page) {
		super();
		this.ids = List.copyOf(ObjectHelper.getDefaultValue(ids, List.of()));
		this.status = TextHelper.getDefaultValue(status);
		this.page = ObjectHelper.getDefaultValue(page, PageCriteria.defaultPage());
	}

	public static BookCriteria byIds(final List<UUID> ids) {
		return new BookCriteria(ids, "", PageCriteria.defaultPage());
	}

	public static BookCriteria active(final PageCriteria page) {
		return new BookCriteria(List.of(), BookEntity.STATUS_ACTIVE, page);
	}

	public List<UUID> getIds() {
		return ids;
	}

	public String getStatus() {
		return status;
	}

	public PageCriteria getPage() {
		return page;
	}

	public boolean filtersByIds() {
		return !ids.isEmpty();
	}

}
