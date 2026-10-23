package co.com.bookhive.circulation.domain.loan.returnbook;

import java.math.BigDecimal;

public interface FinePolicy {

	BigDecimal dailyRate();

	BigDecimal amountFor(int overdueDays);

}
