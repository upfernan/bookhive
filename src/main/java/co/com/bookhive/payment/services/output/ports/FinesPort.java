package co.com.bookhive.payment.services.output.ports;

import java.math.BigDecimal;
import java.util.UUID;

import co.com.bookhive.payment.services.output.ports.entities.PayableFineEntity;
import co.com.bookhive.payment.services.output.ports.entities.SettledFineEntity;

public interface FinesPort {

	PayableFineEntity find(UUID fine, UUID payer);

	SettledFineEntity settle(UUID fine, String paymentReference, BigDecimal amount);

}
