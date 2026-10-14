package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.transversal.excepcion.BookHiveException;

class ValidadorFechaPrestamoTest {

	@Test
	@DisplayName("pasa cuando la fecha es la de hoy en UTC")
	void pasaCuandoLaFechaEsLaDeHoyEnUtc() {
		assertThatCode(() -> ValidadorFechaPrestamo.ejecutarValidacion(PrestarLibroDominioFabrica.valido()))
				.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("falla cuando la fecha no viene")
	void fallaCuandoLaFechaNoViene() {
		assertThatThrownBy(() -> ValidadorFechaPrestamo.ejecutarValidacion(PrestarLibroDominioFabrica.sinFecha()))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("fecha del préstamo");
	}

	@Test
	@DisplayName("falla cuando la fecha es futura")
	void fallaCuandoLaFechaEsFutura() {
		assertThatThrownBy(() -> ValidadorFechaPrestamo.ejecutarValidacion(PrestarLibroDominioFabrica.conFechaFutura()))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("posterior a la fecha actual");
	}

}
