package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.transversal.excepcion.BookHiveException;

class ValidadorEjemplarTest {

	@Test
	@DisplayName("pasa cuando el ejemplar viene")
	void pasaCuandoElEjemplarViene() {
		assertThatCode(() -> ValidadorEjemplar.ejecutarValidacion(PrestarLibroDominioFabrica.valido()))
				.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("falla cuando el ejemplar no viene")
	void fallaCuandoElEjemplarNoViene() {
		assertThatThrownBy(() -> ValidadorEjemplar.ejecutarValidacion(PrestarLibroDominioFabrica.sinEjemplar()))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("ejemplar");
	}

}
