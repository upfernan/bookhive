package co.com.bookhive.payment.testdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.services.output.ports.FinesPort;
import co.com.bookhive.payment.services.output.ports.PaymentGatewayPort;
import co.com.bookhive.payment.services.output.ports.SessionContextPort;
import co.com.bookhive.payment.services.output.ports.TenantScopePort;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutEntity;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutRequestEntity;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;
import co.com.bookhive.payment.services.output.ports.entities.PayableFineEntity;
import co.com.bookhive.payment.services.output.ports.entities.SettledFineEntity;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentEventRepository;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentRepository;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEventEntity;

public final class FakePayments {

	private FakePayments() {
		super();
	}

	public static FinePaymentEntity payment(final String reference, final UUID fine, final UUID payer, final long cents) {
		final FinePaymentEntity payment = new FinePaymentEntity();
		payment.setId(UUID.randomUUID());
		payment.setReference(reference);
		payment.setFine(fine);
		payment.setPayer(payer);
		payment.setInstitution(UUID.randomUUID());
		payment.setAmountInCents(cents);
		payment.setCurrency("COP");
		payment.setCreatedAt(Instant.parse("2026-10-26T12:00:00Z"));
		payment.setExpiresAt(Instant.parse("2026-10-26T12:30:00Z"));
		return payment;
	}

	public static final class Payments implements FinePaymentRepository {

		private final List<FinePaymentEntity> stored = new ArrayList<>();
		private final List<FinePaymentEntity> created = new ArrayList<>();
		private final List<FinePaymentCriteria> searches = new ArrayList<>();

		public Payments with(final FinePaymentEntity payment) {
			stored.add(payment);
			return this;
		}

		public List<FinePaymentEntity> created() {
			return created;
		}

		public List<FinePaymentCriteria> searches() {
			return searches;
		}

		@Override
		public void create(final FinePaymentEntity entity) {
			created.add(entity);
		}

		@Override
		public List<FinePaymentEntity> findByCriteria(final FinePaymentCriteria criteria) {
			searches.add(criteria);
			return stored.stream()
					.filter(payment -> !criteria.filtersByReference() || payment.getReference().equals(criteria.getReference()))
					.filter(payment -> !criteria.filtersByFine() || payment.getFine().equals(criteria.getFine()))
					.filter(payment -> !criteria.filtersByExpiration()
							|| payment.getExpiresAt().isAfter(criteria.getExpiresAfter()))
					.toList();
		}

	}

	public static final class Events implements FinePaymentEventRepository {

		private final List<FinePaymentEventEntity> recorded = new ArrayList<>();

		public List<FinePaymentEventEntity> recorded() {
			return recorded;
		}

		@Override
		public boolean register(final FinePaymentEventEntity event) {
			if (recorded.stream().anyMatch(existing -> existing.getTransaction().equals(event.getTransaction())
					&& existing.getStatus().equals(event.getStatus()))) {
				return false;
			}
			recorded.add(event);
			return true;
		}

	}

	public static final class Fines implements FinesPort {

		private final List<String> settlements = new ArrayList<>();
		private PayableFineEntity payable = new PayableFineEntity(null, null, false);
		private String settledBy = "";

		public Fines payable(final UUID fine, final BigDecimal amount) {
			payable = new PayableFineEntity(fine, amount, true);
			return this;
		}

		public Fines alreadyPaid(final UUID fine, final BigDecimal amount) {
			payable = new PayableFineEntity(fine, amount, false);
			return this;
		}

		public Fines settledBy(final String reference) {
			settledBy = reference;
			return this;
		}

		public List<String> settlements() {
			return settlements;
		}

		@Override
		public PayableFineEntity find(final UUID fine, final UUID payer) {
			return payable;
		}

		@Override
		public SettledFineEntity settle(final UUID fine, final String paymentReference, final BigDecimal amount) {
			settlements.add(fine + " " + paymentReference + " " + amount);
			if (!settledBy.isEmpty()) {
				return new SettledFineEntity(false, settledBy);
			}
			settledBy = paymentReference;
			return new SettledFineEntity(true, paymentReference);
		}

	}

	public static final class Gateway implements PaymentGatewayPort {

		private final List<GatewayTransactionEntity> transactions = new ArrayList<>();
		private final List<CheckoutRequestEntity> checkouts = new ArrayList<>();
		private boolean authentic = true;
		private boolean down;

		public Gateway with(final GatewayTransactionEntity transaction) {
			transactions.add(transaction);
			return this;
		}

		public Gateway notAuthentic() {
			authentic = false;
			return this;
		}

		public Gateway down() {
			down = true;
			return this;
		}

		public List<CheckoutRequestEntity> checkouts() {
			return checkouts;
		}

		@Override
		public CheckoutEntity checkoutFor(final CheckoutRequestEntity request) {
			checkouts.add(request);
			return new CheckoutEntity("https://checkout.wompi.co/p/", "pub_test_1", "firma-" + request.reference(),
					"2026-10-26T12:30:00.000Z", "https://bookhive.test/pagos");
		}

		@Override
		public Optional<GatewayTransactionEntity> findTransaction(final String transaction) {
			failIfDown();
			return transactions.stream().filter(found -> found.id().equals(transaction)).findFirst();
		}

		@Override
		public List<GatewayTransactionEntity> findTransactionsOf(final String reference, final Instant createdAt) {
			failIfDown();
			return transactions.stream().filter(found -> found.reference().equals(reference)).toList();
		}

		@Override
		public boolean isAuthentic(final List<String> signedValues, final long timestamp, final String checksum) {
			return authentic;
		}

		private void failIfDown() {
			if (down) {
				throw BookHiveException.createUnavailable(
						new IllegalStateException("caído"), MessageKey.PAYMENT_GATEWAY_UNAVAILABLE, "Wompi caído.");
			}
		}

	}

	public static final class Session implements SessionContextPort {

		private final UUID institution;
		private final UUID person;
		private final String schema;

		public Session(final UUID institution, final UUID person, final String schema) {
			this.institution = institution;
			this.person = person;
			this.schema = schema;
		}

		@Override
		public UUID getInstitution() {
			return institution;
		}

		@Override
		public UUID getPerson() {
			return person;
		}

		@Override
		public String getSchema() {
			return schema;
		}

	}

	public static final class Tenants implements TenantScopePort {

		private final List<String> visited = new ArrayList<>();

		public List<String> visited() {
			return visited;
		}

		@Override
		public <T> T within(final String schema, final Supplier<T> action) {
			visited.add(schema);
			return action.get();
		}

	}

}
