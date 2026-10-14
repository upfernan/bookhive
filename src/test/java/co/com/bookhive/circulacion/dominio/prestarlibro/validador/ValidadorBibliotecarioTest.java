package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.transversal.excepcion.BookHiveException;

class ValidadorBibliotecarioTest {

	@Test
	@DisplayName("pasa cuando el bibliotecario viene")
	void pasaCuandoElBibliotecarioViene() {
		assertThatCode(() -> ValidadorBibliotecario.ejecutarValidacion(PrestarLibroDominioFabrica.valido()))
				.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("falla cuando el bibliotecario no se pudo resolver desde el token")
	void fallaCuandoElBibliotecarioNoViene() {
		assertThatThrownBy(() -> ValidadorBibliotecario.ejecutarValidacion(PrestarLibroDominioFabrica.sinBibliotecario()))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("bibliotecario");
	}

}
