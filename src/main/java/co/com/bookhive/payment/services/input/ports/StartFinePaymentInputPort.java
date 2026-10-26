package co.com.bookhive.payment.services.input.ports;

import co.com.bookhive.payment.services.input.dto.request.StartFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentCheckoutResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface StartFinePaymentInputPort
		extends InputPortWithReturn<StartFinePaymentRequestDTO, FinePaymentCheckoutResponseDTO> {
}
