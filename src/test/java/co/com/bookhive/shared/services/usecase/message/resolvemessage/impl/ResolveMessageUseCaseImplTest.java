package co.com.bookhive.shared.services.usecase.message.resolvemessage.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.shared.domain.message.resolvemessage.ResolveMessageDomain;

class ResolveMessageUseCaseImplTest {

	@Test
	@DisplayName("busca en el catálogo el texto de la clave pedida")
	void resolvesTextFromCatalog() {
		final List<String> askedKeys = new ArrayList<>();
		final ResolveMessageUseCaseImpl useCase = new ResolveMessageUseCaseImpl(key -> {
			askedKeys.add(key);
			return "Texto de " + key;
		});

		final ResolveMessageDomain message = useCase.execute(new ResolveMessageDomain("  loan.unexpected  ", ""));

		assertEquals(List.of("loan.unexpected"), askedKeys);
		assertEquals("loan.unexpected", message.key());
		assertEquals("Texto de loan.unexpected", message.text());
	}

	@Test
	@DisplayName("si no llega mensaje le pide al catálogo la clave vacía")
	void resolvesMissingMessage() {
		final ResolveMessageUseCaseImpl useCase = new ResolveMessageUseCaseImpl(key -> "Mensaje general" + key);

		assertEquals("Mensaje general", useCase.execute(null).text());
	}

}
