package co.com.bookhive.access.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.input.dto.request.AuthorizeOperationRequestDTO;
import co.com.bookhive.crosscutting.security.PermissionCode;

class AuthorizeOperationInputAdapterTest {

	private final List<String> received = new ArrayList<>();

	private final AuthorizeOperationInputAdapter adapter = new AuthorizeOperationInputAdapter(
			operation -> received.add(operation.permission() + " " + operation.operation()));

	@Test
	@DisplayName("le pasa al caso de uso el permiso y la operación")
	void passesOperationToUseCase() {
		adapter.execute(new AuthorizeOperationRequestDTO(PermissionCode.LEND_BOOK, "LoanController#lend"));
		adapter.execute(null);

		assertEquals(List.of(PermissionCode.LEND_BOOK + " LoanController#lend", " "), received);
	}

}
