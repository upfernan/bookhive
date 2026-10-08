package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.transversal.excepcion.BookHiveException;

class ValidadorInstitucionTest {

	@Test
	@DisplayName("pasa cuando la institución viene")
	void pasaCuandoLaInstitucionViene() {
		assertThatCode(() -> ValidadorInstitucion.ejecutarValidacion(PrestarLibroDominioFabrica.valido()))
				.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("falla cuando la institución no se pudo resolver desde el token")
	void fallaCuandoLaInstitucionNoViene() {
		assertThatThrownBy(() -> ValidadorInstitucion.ejecutarValidacion(PrestarLibroDominioFabrica.sinInstitucion()))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("institución");
	}

}
