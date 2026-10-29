package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class ReservationCriteria {

	private static final List<String> ACTIVE = List.of(ReservationEntity.STATUS_WAITING, ReservationEntity.STATUS_ASSIGNED);
	private static final List<String> ASSIGNED = List.of(ReservationEntity.STATUS_ASSIGNED);

	private final UUID user;
	private final UUID book;
	private final UUID bookCopy;
	private final LocalDate pickupBefore;
	private final List<String> statuses;

	private ReservationCriteria(
			final UUID user,
			final UUID book,
			final UUID bookCopy,
			final LocalDate pickupBefore,
			final List<String> statuses) {

		super();
		this.user = UUIDHelper.getDefaultValue(user);
		this.book = UUIDHelper.getDefaultValue(book);
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		this.pickupBefore = DateHelper.getDefaultValue(pickupBefore);
		this.statuses = List.copyOf(ObjectHelper.getDefaultValue(statuses, ACTIVE));
	}

	public static ReservationCriteria activeOfUser(final UUID user) {
		return new ReservationCriteria(user, null, null, null, ACTIVE);
	}

	public static ReservationCriteria activeOfUserForBook(final UUID user, final UUID book) {
		return new ReservationCriteria(user, book, null, null, ACTIVE);
	}

	public static ReservationCriteria waitingForBook(final UUID book) {
		return new ReservationCriteria(null, book, null, null, List.of(ReservationEntity.STATUS_WAITING));
	}

	public static ReservationCriteria assignedToBookCopy(final UUID bookCopy) {
		return new ReservationCriteria(null, null, bookCopy, null, ASSIGNED);
	}

	public static ReservationCriteria assignedWithPickupBefore(final LocalDate date) {
		return new ReservationCriteria(null, null, null, date, ASSIGNED);
	}

	public UUID getUser() {
		return user;
	}

	public UUID getBook() {
		return book;
	}

	public UUID getBookCopy() {
		return bookCopy;
	}

	public LocalDate getPickupBefore() {
		return pickupBefore;
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

	public boolean filtersByBookCopy() {
		return !UUIDHelper.isDefaultValue(bookCopy);
	}

	public boolean filtersByPickupBefore() {
		return !DateHelper.isDefaultValue(pickupBefore);
	}

	public boolean isComplete() {
		return filtersByUser() || filtersByBook() || filtersByBookCopy() || filtersByPickupBefore();
	}

}
