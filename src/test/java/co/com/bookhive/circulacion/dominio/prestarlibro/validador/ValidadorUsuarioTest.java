package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.transversal.excepcion.BookHiveException;

class ValidadorUsuarioTest {

	@Test
	@DisplayName("pasa cuando el usuario viene")
	void pasaCuandoElUsuarioViene() {
		assertThatCode(() -> ValidadorUsuario.ejecutarValidacion(PrestarLibroDominioFabrica.valido()))
				.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("falla cuando el usuario no viene")
	void fallaCuandoElUsuarioNoViene() {
		assertThatThrownBy(() -> ValidadorUsuario.ejecutarValidacion(PrestarLibroDominioFabrica.sinUsuario()))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("usuario");
	}

}
