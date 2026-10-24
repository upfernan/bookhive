package co.com.bookhive.circulation.testdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanReturnRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanReturnCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

public final class FakeRepositories {

	private FakeRepositories() {
		super();
	}

	public static BookCopyEntity bookCopy(final UUID id, final String status, final boolean lendable) {
		final BookCopyEntity bookCopy = new BookCopyEntity();
		bookCopy.setId(id);
		bookCopy.setStatus(status);
		bookCopy.setLendable(lendable);
		return bookCopy;
	}

	public static BookCopyEntity bookCopyOf(final UUID book, final String status) {
		final BookCopyEntity bookCopy = bookCopy(UUID.randomUUID(), status, true);
		bookCopy.setBook(book);
		return bookCopy;
	}

	public static UserEntity user(final UUID id, final String name, final String email) {
		final UserEntity user = new UserEntity();
		user.setId(id);
		user.setName(name);
		user.setEmail(email);
		return user;
	}

	public static final class Users implements UserRepository {

		private final List<UserEntity> stored = new ArrayList<>();

		public Users with(final UserEntity user) {
			stored.add(user);
			return this;
		}

		@Override
		public Optional<UserEntity> findById(final UUID id) {
			return stored.stream().filter(user -> user.getId().equals(id)).findFirst();
		}

		@Override
		public List<UserEntity> findAll(final PageCriteria page) {
			return List.copyOf(stored);
		}

		@Override
		public List<UserEntity> findByCriteria(final UserCriteria criteria) {
			return stored.stream()
					.filter(user -> user.getEmail().toLowerCase(Locale.ROOT).equals(criteria.getEmail()))
					.toList();
		}

	}

	public static final class BookCopies implements BookCopyRepository {

		private final List<BookCopyEntity> stored = new ArrayList<>();
		private final List<BookCopyEntity> updated = new ArrayList<>();
		private final List<String> statusChanges = new ArrayList<>();
		private boolean changedByOther;

		public BookCopies with(final BookCopyEntity bookCopy) {
			stored.add(bookCopy);
			return this;
		}

		public BookCopies changedByOtherRequest() {
			changedByOther = true;
			return this;
		}

		public List<BookCopyEntity> updated() {
			return updated;
		}

		public List<String> statusChanges() {
			return statusChanges;
		}

		@Override
		public Optional<BookCopyEntity> findById(final UUID id) {
			return stored.stream().filter(bookCopy -> bookCopy.getId().equals(id)).findFirst();
		}

		@Override
		public List<BookCopyEntity> findAll(final PageCriteria page) {
			return List.copyOf(stored);
		}

		@Override
		public List<BookCopyEntity> findByCriteria(final BookCopyCriteria criteria) {
			return stored.stream()
					.filter(bookCopy -> criteria.filtersByBarcode()
							? criteria.getBarcode().equals(bookCopy.getBarcode())
							: criteria.getBooks().contains(bookCopy.getBook()))
					.filter(bookCopy -> !bookCopy.getStatus().equals(criteria.getExcludedStatus()))
					.toList();
		}

		@Override
		public void update(final BookCopyEntity entity) {
			updated.add(entity);
		}

		@Override
		public boolean changeStatus(final UUID bookCopy, final String expectedStatus, final String newStatus) {
			if (changedByOther) {
				return false;
			}
			final Optional<BookCopyEntity> found = findById(bookCopy)
					.filter(copy -> copy.getStatus().equals(expectedStatus));
			found.ifPresent(copy -> {
				copy.setStatus(newStatus);
				statusChanges.add(bookCopy + ":" + expectedStatus + "->" + newStatus);
			});
			return found.isPresent();
		}

	}

	public static final class Fines implements FineRepository {

		private final List<FineEntity> pending = new ArrayList<>();
		private final List<FineEntity> created = new ArrayList<>();

		public Fines withPendingFine() {
			pending.add(new FineEntity());
			return this;
		}

		public Fines withFine(final FineEntity fine) {
			pending.add(fine);
			return this;
		}

		public List<FineEntity> created() {
			return created;
		}

		@Override
		public void create(final FineEntity entity) {
			created.add(entity);
		}

		@Override
		public List<FineEntity> findByCriteria(final FineCriteria criteria) {
			if (criteria.filtersByLoan()) {
				return pending.stream().filter(fine -> fine.getLoan().getId().equals(criteria.getLoan())).toList();
			}
			return List.copyOf(pending);
		}

	}

	public static final class Loans implements LoanRepository {

		private final List<LoanEntity> active = new ArrayList<>();
		private final List<LoanEntity> created = new ArrayList<>();
		private final List<LoanEntity> stored = new ArrayList<>();

		private final List<LoanCriteria> searches = new ArrayList<>();

		public Loans withActive(final LoanEntity loan) {
			active.add(loan);
			return this;
		}

		public List<LoanCriteria> searches() {
			return searches;
		}

		public Loans withActiveLoans(final int amount) {
			for (int i = 0; i < amount; i++) {
				active.add(new LoanEntity());
			}
			return this;
		}

		public List<LoanEntity> created() {
			return created;
		}

		@Override
		public void create(final LoanEntity entity) {
			created.add(entity);
		}

		public Loans withReturned(final LoanEntity loan) {
			stored.add(loan);
			return this;
		}

		@Override
		public Optional<LoanEntity> findById(final UUID id) {
			return Stream.of(created, active, stored)
					.flatMap(List::stream)
					.filter(loan -> loan.getId().equals(id))
					.findFirst();
		}

		@Override
		public List<LoanEntity> findAll(final PageCriteria page) {
			return List.copyOf(created);
		}

		@Override
		public List<LoanEntity> findByCriteria(final LoanCriteria criteria) {
			searches.add(criteria);
			return List.copyOf(active);
		}

	}

	public static final class Parameters implements InstitutionParametersRepository {

		private final List<InstitutionParametersEntity> stored = new ArrayList<>();

		public Parameters with(final int loanDays, final int maxConcurrentLoans) {
			return with(loanDays, maxConcurrentLoans, BigDecimal.ZERO);
		}

		public Parameters with(final int loanDays, final int maxConcurrentLoans, final BigDecimal dailyFineRate) {
			return with(loanDays, maxConcurrentLoans, dailyFineRate, InstitutionParametersEntity.DEFAULT_TIME_ZONE);
		}

		public Parameters with(
				final int loanDays,
				final int maxConcurrentLoans,
				final BigDecimal dailyFineRate,
				final String timeZone) {
			final InstitutionParametersEntity parameters = new InstitutionParametersEntity();
			parameters.setLoanDays(loanDays);
			parameters.setMaxConcurrentLoans(maxConcurrentLoans);
			parameters.setDailyFineRate(dailyFineRate);
			parameters.setTimeZone(timeZone);
			stored.add(parameters);
			return this;
		}

		@Override
		public Optional<InstitutionParametersEntity> findById(final UUID id) {
			return stored.stream().findFirst();
		}

		@Override
		public List<InstitutionParametersEntity> findAll(final PageCriteria page) {
			return List.copyOf(stored);
		}

	}

	public static ReservationEntity reservation(
			final UUID book, final UUID user, final String status, final Instant requestedAt) {

		final ReservationEntity reservation = new ReservationEntity();
		reservation.setId(UUID.randomUUID());
		reservation.setBook(book);
		reservation.setUser(user);
		reservation.setStatus(status);
		reservation.setRequestedAt(requestedAt);
		return reservation;
	}

	public static final class Reservations implements ReservationRepository {

		private final List<ReservationEntity> stored = new ArrayList<>();
		private final List<ReservationEntity> created = new ArrayList<>();
		private final List<ReservationCriteria> searches = new ArrayList<>();
		private String statusSetByOther = "";

		public Reservations with(final ReservationEntity reservation) {
			stored.add(reservation);
			return this;
		}

		public Reservations changedByOtherRequestTo(final String status) {
			statusSetByOther = status;
			return this;
		}

		public List<ReservationEntity> created() {
			return created;
		}

		public List<ReservationCriteria> searches() {
			return searches;
		}

		@Override
		public void create(final ReservationEntity entity) {
			created.add(entity);
			stored.add(entity);
		}

		@Override
		public Optional<ReservationEntity> findById(final UUID id) {
			return stored.stream().filter(reservation -> reservation.getId().equals(id)).findFirst();
		}

		@Override
		public List<ReservationEntity> findAll(final PageCriteria page) {
			return List.copyOf(stored);
		}

		@Override
		public List<ReservationEntity> findByCriteria(final ReservationCriteria criteria) {
			searches.add(criteria);
			return stored.stream()
					.filter(reservation -> !criteria.filtersByUser() || reservation.getUser().equals(criteria.getUser()))
					.filter(reservation -> !criteria.filtersByBook() || reservation.getBook().equals(criteria.getBook()))
					.filter(reservation -> criteria.getStatuses().contains(reservation.getStatus()))
					.toList();
		}

		@Override
		public boolean changeStatus(final UUID reservation, final String expectedStatus, final String newStatus) {
			if (!statusSetByOther.isEmpty()) {
				findById(reservation).ifPresent(found -> found.setStatus(statusSetByOther));
				return false;
			}
			final Optional<ReservationEntity> found = findById(reservation)
					.filter(entity -> entity.getStatus().equals(expectedStatus));
			found.ifPresent(entity -> entity.setStatus(newStatus));
			return found.isPresent();
		}

	}

	public static final class LoanReturns implements LoanReturnRepository {

		private final List<LoanReturnEntity> created = new ArrayList<>();
		private final List<LoanReturnEntity> stored = new ArrayList<>();

		public LoanReturns with(final LoanReturnEntity loanReturn) {
			stored.add(loanReturn);
			return this;
		}

		public List<LoanReturnEntity> created() {
			return created;
		}

		@Override
		public void create(final LoanReturnEntity entity) {
			created.add(entity);
		}

		@Override
		public List<LoanReturnEntity> findByCriteria(final LoanReturnCriteria criteria) {
			return stored.stream()
					.filter(loanReturn -> loanReturn.getReturnDate().equals(criteria.getReturnDate()))
					.toList();
		}

	}

}
