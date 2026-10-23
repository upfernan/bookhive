package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.loan.returnbook.ReturnBookDomain;
import co.com.bookhive.circulation.domain.loan.returnbook.ReturnedLoanDomain;
import co.com.bookhive.circulation.services.input.dto.request.ReturnBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReturnBookResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ReturnBookInputAdapterTest {

	private static final UUID LOAN = UUID.randomUUID();
	private static final UUID BOOK_COPY = UUID.randomUUID();
	private static final UUID FINE = UUID.randomUUID();
	private static final LocalDate TODAY = DateHelper.todayInUtc();

	@Test
	@DisplayName("pasa el ejemplar con la fecha de hoy y devuelve el detalle de la multa")
	void mapsRequestAndResponse() {
		final List<ReturnBookDomain> received = new ArrayList<>();
		final ReturnBookInputAdapter adapter = new ReturnBookInputAdapter(domain -> {
			received.add(domain);
			return new ReturnedLoanDomain(LOAN, BOOK_COPY, TODAY.minusDays(2), TODAY, 2, FINE,
					new BigDecimal("1000.00"), new BigDecimal("2000.00"));
		});

		final ReturnBookResponseDTO response = adapter.execute(new ReturnBookRequestDTO(BOOK_COPY));

		assertEquals(BOOK_COPY, received.get(0).getBookCopy());
		assertEquals(TODAY, received.get(0).getReturnDate());
		assertEquals(new ReturnBookResponseDTO(LOAN, BOOK_COPY, TODAY.minusDays(2), TODAY, 2, true, FINE,
				new BigDecimal("1000.00"), new BigDecimal("2000.00")), response);
	}

	@Test
	@DisplayName("una petición nula o un caso de uso sin respuesta se rechazan")
	void rejectsNulls() {
		final ReturnBookInputAdapter withoutAnswer = new ReturnBookInputAdapter(domain -> null);
		final ReturnBookRequestDTO request = new ReturnBookRequestDTO(BOOK_COPY);

		assertEquals(MessageKey.LOAN_DATA_REQUIRED,
				assertThrows(BookHiveException.class, () -> withoutAnswer.execute(null)).getUserMessageKey());
		assertEquals(MessageKey.LOAN_RETURN_NOT_CONFIRMED,
				assertThrows(BookHiveException.class, () -> withoutAnswer.execute(request)).getUserMessageKey());
	}

}
