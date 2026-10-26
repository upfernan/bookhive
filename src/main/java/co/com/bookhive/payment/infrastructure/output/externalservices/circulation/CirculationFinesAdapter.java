package co.com.bookhive.payment.infrastructure.output.externalservices.circulation;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.services.input.dto.request.FindPayableFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.SettleFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.PayableFineResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.SettleFineResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindPayableFineInputPort;
import co.com.bookhive.circulation.services.input.ports.SettleFineInputPort;
import co.com.bookhive.payment.services.output.ports.FinesPort;
import co.com.bookhive.payment.services.output.ports.entities.PayableFineEntity;
import co.com.bookhive.payment.services.output.ports.entities.SettledFineEntity;

@Component
public class CirculationFinesAdapter implements FinesPort {

	private final FindPayableFineInputPort findPayableFine;
	private final SettleFineInputPort settleFine;

	public CirculationFinesAdapter(final FindPayableFineInputPort findPayableFine, final SettleFineInputPort settleFine) {
		super();
		this.findPayableFine = findPayableFine;
		this.settleFine = settleFine;
	}

	@Override
	public PayableFineEntity find(final UUID fine, final UUID payer) {
		final PayableFineResponseDTO found = findPayableFine.execute(new FindPayableFineRequestDTO(fine, payer));
		return new PayableFineEntity(found.fine(), found.amount(), found.pending());
	}

	@Override
	public SettledFineEntity settle(final UUID fine, final String paymentReference, final BigDecimal amount) {
		final SettleFineResponseDTO settled = settleFine.execute(new SettleFineRequestDTO(fine, paymentReference, amount));
		return new SettledFineEntity(settled.newlySettled(), settled.paymentReference());
	}

}
