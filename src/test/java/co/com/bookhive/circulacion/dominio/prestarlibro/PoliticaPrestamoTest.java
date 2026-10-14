package co.com.bookhive.circulacion.dominio.prestarlibro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.FechaHelper;

class PoliticaPrestamoTest {

	@Test
	@DisplayName("suma a la fecha del préstamo los días que configuró la institución")
	void sumaLosDiasConfiguradosPorLaInstitucion() {

		final LocalDate hoy = FechaHelper.hoyEnUtc();

		assertThat(PoliticaPrestamo.calcularFechaVencimiento(hoy, 15))
				.isEqualTo(hoy.plusDays(15));
	}

	@Test
	@DisplayName("dos instituciones con plazos distintos dan vencimientos distintos")
	void dosInstitucionesConPlazosDistintosDanVencimientosDistintos() {

		final LocalDate hoy = FechaHelper.hoyEnUtc();

		assertThat(PoliticaPrestamo.calcularFechaVencimiento(hoy, 8))
				.isNotEqualTo(PoliticaPrestamo.calcularFechaVencimiento(hoy, 21));
	}

	@Test
	@DisplayName("falla cuando la institución no tiene plazo configurado")
	void fallaCuandoLaInstitucionNoTienePlazoConfigurado() {

		assertThatThrownBy(() -> PoliticaPrestamo.calcularFechaVencimiento(FechaHelper.hoyEnUtc(), 0))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("mayor que cero");
	}

	@Test
	@DisplayName("falla cuando el plazo configurado es negativo")
	void fallaCuandoElPlazoEsNegativo() {

		assertThatThrownBy(() -> PoliticaPrestamo.calcularFechaVencimiento(FechaHelper.hoyEnUtc(), -5))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("mayor que cero");
	}

	@Test
	@DisplayName("falla cuando no hay fecha de préstamo")
	void fallaCuandoNoHayFechaDePrestamo() {

		assertThatThrownBy(() -> PoliticaPrestamo.calcularFechaVencimiento(
				FechaHelper.obtenerValorDefecto(), 15))
				.isInstanceOf(BookHiveException.class)
				.hasMessageContaining("sin fecha de préstamo");
	}

}
