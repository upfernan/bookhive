package co.com.bookhive.circulation.testdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanReturnRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;
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

		public BookCopies with(final BookCopyEntity bookCopy) {
			stored.add(bookCopy);
			return this;
		}

		public List<BookCopyEntity> updated() {
			return updated;
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

	}

	public static final class Fines implements FineRepository {

		private final List<FineEntity> pending = new ArrayList<>();
		private final List<FineEntity> created = new ArrayList<>();

		public Fines withPendingFine() {
			pending.add(new FineEntity());
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
			return List.copyOf(pending);
		}

	}

	public static final class Loans implements LoanRepository {

		private final List<LoanEntity> active = new ArrayList<>();
		private final List<LoanEntity> created = new ArrayList<>();
		private final List<LoanEntity> updated = new ArrayList<>();

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

		public List<LoanEntity> updated() {
			return updated;
		}

		@Override
		public void update(final LoanEntity entity) {
			updated.add(entity);
		}

		@Override
		public Optional<LoanEntity> findById(final UUID id) {
			return created.stream().filter(loan -> loan.getId().equals(id)).findFirst();
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
			final InstitutionParametersEntity parameters = new InstitutionParametersEntity();
			parameters.setLoanDays(loanDays);
			parameters.setMaxConcurrentLoans(maxConcurrentLoans);
			parameters.setDailyFineRate(dailyFineRate);
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

	public static final class LoanReturns implements LoanReturnRepository {

		private final List<LoanReturnEntity> created = new ArrayList<>();

		public List<LoanReturnEntity> created() {
			return created;
		}

		@Override
		public void create(final LoanReturnEntity entity) {
			created.add(entity);
		}

	}

}
