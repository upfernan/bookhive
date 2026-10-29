package co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class TenantSessionTest {

	@Test
	@DisplayName("cada institución tiene su propio rol de base de datos")
	void namesInstitutionRole() {
		assertEquals("inst_uco", TenantSession.roleOf("uco"));
	}

	@Test
	@DisplayName("no arma un rol con un nombre de esquema que pueda dañar la consulta")
	void rejectsUnsafeSchema() {
		assertThrows(BookHiveException.class, () -> TenantSession.roleOf("uco; DROP ROLE inst_uco"));
	}

}
