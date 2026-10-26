package co.com.bookhive.payment.services.output.ports;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import co.com.bookhive.payment.services.output.ports.entities.CheckoutEntity;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutRequestEntity;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;

public interface PaymentGatewayPort {

	CheckoutEntity checkoutFor(CheckoutRequestEntity request);

	Optional<GatewayTransactionEntity> findTransaction(String transaction);

	List<GatewayTransactionEntity> findTransactionsOf(String reference, Instant createdAt);

	boolean isAuthentic(List<String> signedValues, long timestamp, String checksum);

}
