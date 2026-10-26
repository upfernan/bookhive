package co.com.bookhive.payment.services.input.ports;

import co.com.bookhive.payment.services.input.dto.request.PaymentEventRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.PaymentEventResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface RecordPaymentEventInputPort
		extends InputPortWithReturn<PaymentEventRequestDTO, PaymentEventResponseDTO> {
}
