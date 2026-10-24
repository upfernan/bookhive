package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class ReservationCriteria {

	private static final List<String> ACTIVE = List.of(ReservationEntity.STATUS_WAITING, ReservationEntity.STATUS_ASSIGNED);

	private final UUID user;
	private final UUID book;
	private final List<String> statuses;

	private ReservationCriteria(final UUID user, final UUID book, final List<String> statuses) {
		super();
		this.user = UUIDHelper.getDefaultValue(user);
		this.book = UUIDHelper.getDefaultValue(book);
		this.statuses = List.copyOf(ObjectHelper.getDefaultValue(statuses, ACTIVE));
	}

	public static ReservationCriteria activeOfUser(final UUID user) {
		return new ReservationCriteria(user, UUIDHelper.getDefaultValue(), ACTIVE);
	}

	public static ReservationCriteria activeOfUserForBook(final UUID user, final UUID book) {
		return new ReservationCriteria(user, book, ACTIVE);
	}

	public static ReservationCriteria waitingForBook(final UUID book) {
		return new ReservationCriteria(UUIDHelper.getDefaultValue(), book, List.of(ReservationEntity.STATUS_WAITING));
	}

	public UUID getUser() {
		return user;
	}

	public UUID getBook() {
		return book;
	}

	public List<String> getStatuses() {
		return statuses;
	}

	public boolean filtersByUser() {
		return !UUIDHelper.isDefaultValue(user);
	}

	public boolean filtersByBook() {
		return !UUIDHelper.isDefaultValue(book);
	}

	public boolean isComplete() {
		return filtersByUser() || filtersByBook();
	}

}
