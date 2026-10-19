package co.com.bookhive.circulation.services.usecase.lendbook.impl.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class LoanDomainMapperImplTest {

	private final LoanDomainMapper mapper = new LoanDomainMapperImpl();

	@Test
	@DisplayName("convierte el dominio en la entidad del préstamo activo")
	void mapsDomainToEntity() {
		final UUID id = UUIDHelper.generateNew();
		final UUID bookCopy = UUIDHelper.generateNew();
		final UUID user = UUIDHelper.generateNew();
		final UUID librarian = UUIDHelper.generateNew();
		final LocalDate loanDate = LocalDate.of(2026, 10, 17);
		final LocalDate dueDate = loanDate.plusDays(15);

		final LoanEntity entity = mapper.toEntity(
				new LendBookDomain(id, bookCopy, user, UUIDHelper.generateNew(), librarian, loanDate, dueDate));

		assertEquals(id, entity.getId());
		assertEquals(bookCopy, entity.getBookCopy().getId());
		assertEquals(user, entity.getUser().getId());
		assertEquals(librarian, entity.getLibrarian());
		assertEquals(loanDate, entity.getLoanDate());
		assertEquals(dueDate, entity.getDueDate());
		assertEquals(LoanEntity.STATUS_ACTIVE, entity.getStatus());
	}

	@Test
	@DisplayName("devuelve una entidad con valores por defecto si el dominio es nulo")
	void mapsNullToDefaultEntity() {
		final LoanEntity entity = mapper.toEntity(null);

		assertEquals(UUIDHelper.getDefaultValue(), entity.getId());
		assertEquals(UUIDHelper.getDefaultValue(), entity.getBookCopy().getId());
	}

}
