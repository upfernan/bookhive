package co.com.bookhive.payment.services.input.ports;

import co.com.bookhive.payment.services.input.dto.request.ConfirmFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentStatusResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ConfirmFinePaymentInputPort
		extends InputPortWithReturn<ConfirmFinePaymentRequestDTO, FinePaymentStatusResponseDTO> {
}
