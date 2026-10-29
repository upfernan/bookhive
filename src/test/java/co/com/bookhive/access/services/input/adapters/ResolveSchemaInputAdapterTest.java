package co.com.bookhive.access.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.domain.session.resolveschema.ResolveSchemaDomain;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class ResolveSchemaInputAdapterTest {

	@Test
	@DisplayName("entrega el esquema que eligió el caso de uso")
	void returnsSchema() {
		final ResolveSchemaInputAdapter adapter =
				new ResolveSchemaInputAdapter(() -> new ResolveSchemaDomain(UUIDHelper.generateNew(), " uco "));

		assertEquals("uco", adapter.execute().schema());
	}

}
