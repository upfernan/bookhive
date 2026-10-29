package co.com.bookhive.circulation.domain.loan.returnbook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyRateFinePolicyTest {

	@Test
	@DisplayName("cobra la tarifa diaria por cada día de retraso")
	void chargesRatePerOverdueDay() {
		final FinePolicy policy = new DailyRateFinePolicy(new BigDecimal("1000"));

		assertEquals(new BigDecimal("1000.00"), policy.dailyRate());
		assertEquals(new BigDecimal("3000.00"), policy.amountFor(3));
		assertEquals(new BigDecimal("0.00"), policy.amountFor(0));
		assertEquals(new BigDecimal("0.00"), policy.amountFor(-2));
	}

	@Test
	@DisplayName("redondea la tarifa a centavos antes de multiplicar, igual que la guarda la base, y una tarifa vacía o negativa no cobra")
	void roundsAndIgnoresInvalidRates() {
		assertEquals(new BigDecimal("1250.01"), new DailyRateFinePolicy(new BigDecimal("1250.005")).dailyRate());
		assertEquals(new BigDecimal("2500.02"), new DailyRateFinePolicy(new BigDecimal("1250.005")).amountFor(2));
		assertEquals(new BigDecimal("0.00"), new DailyRateFinePolicy(null).amountFor(5));
		assertEquals(new BigDecimal("0.00"), new DailyRateFinePolicy(new BigDecimal("-10")).amountFor(5));
	}

}
