package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.fine.findpayablefine.FindPayableFineDomain;
import co.com.bookhive.circulation.domain.fine.findpayablefine.PayableFineDomain;
import co.com.bookhive.circulation.domain.fine.settlefine.SettleFineDomain;
import co.com.bookhive.circulation.domain.fine.settlefine.SettledFineDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindPayableFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.SettleFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.PayableFineResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.SettleFineResponseDTO;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class FineInputAdaptersTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID USER = UUID.randomUUID();

	@Test
	@DisplayName("la consulta de la multa a pagar pasa la multa y quien paga, y devuelve su valor")
	void findsPayableFine() {
		final List<FindPayableFineDomain> received = new ArrayList<>();
		final FindPayableFineInputAdapter adapter = new FindPayableFineInputAdapter(domain -> {
			received.add(domain);
			return new PayableFineDomain(domain.fine(), new BigDecimal("3000"), true);
		});

		final PayableFineResponseDTO response = adapter.execute(new FindPayableFineRequestDTO(FINE, USER));

		assertEquals(new FindPayableFineDomain(FINE, USER), received.get(0));
		assertEquals(FINE, response.fine());
		assertEquals(new BigDecimal("3000"), response.amount());
		assertTrue(response.pending());
	}

	@Test
	@DisplayName("sin petición consulta con valores por defecto")
	void findsWithDefaultsWithoutRequest() {
		final List<FindPayableFineDomain> received = new ArrayList<>();
		final FindPayableFineInputAdapter adapter = new FindPayableFineInputAdapter(domain -> {
			received.add(domain);
			return new PayableFineDomain(null, null, false);
		});

		final PayableFineResponseDTO response = adapter.execute(null);

		assertEquals(UUIDHelper.getDefaultValue(), received.get(0).fine());
		assertEquals(BigDecimal.ZERO, response.amount());
	}

	@Test
	@DisplayName("saldar la multa pasa la referencia y el valor, y devuelve si quedó saldada ahora")
	void settlesFine() {
		final List<SettleFineDomain> received = new ArrayList<>();
		final SettleFineInputAdapter adapter = new SettleFineInputAdapter(domain -> {
			received.add(domain);
			return new SettledFineDomain(true, domain.paymentReference());
		});

		final SettleFineResponseDTO response =
				adapter.execute(new SettleFineRequestDTO(FINE, "uco-1", new BigDecimal("3000")));

		assertEquals(new SettleFineDomain(FINE, "uco-1", new BigDecimal("3000")), received.get(0));
		assertTrue(response.newlySettled());
		assertEquals("uco-1", response.paymentReference());
	}

	@Test
	@DisplayName("sin petición salda con valores por defecto para que el caso de uso la rechace")
	void settlesWithDefaultsWithoutRequest() {
		final List<SettleFineDomain> received = new ArrayList<>();
		final SettleFineInputAdapter adapter = new SettleFineInputAdapter(domain -> {
			received.add(domain);
			return new SettledFineDomain(false, null);
		});

		final SettleFineResponseDTO response = adapter.execute(null);

		assertEquals("", received.get(0).paymentReference());
		assertEquals("", response.paymentReference());
	}

}
