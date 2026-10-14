package co.com.bookhive.inicializador.trazabilidad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class InstitucionEnRegistroFiltroTest {

	private final InstitucionEnRegistroFiltro filtro = new InstitucionEnRegistroFiltro();

	@AfterEach
	void limpiar() {
		MDC.clear();
	}

	@Test
	@DisplayName("mientras la petición corre, la institución está en el MDC")
	void mientrasLaPeticionCorreLaInstitucionEstaEnElMdc() throws Exception {

		final String[] vistoAdentro = new String[1];

		filtro.doFilter(
				new MockHttpServletRequest(),
				new MockHttpServletResponse(),
				(peticion, respuesta) -> vistoAdentro[0] = MDC.get(InstitucionEnRegistroFiltro.CLAVE_MDC));

		assertThat(vistoAdentro[0]).isEqualTo(InstitucionEnRegistroFiltro.SIN_RESOLVER);
	}

	@Test
	@DisplayName("al terminar la petición el MDC queda limpio")
	void alTerminarLaPeticionElMdcQuedaLimpio() throws Exception {

		filtro.doFilter(
				new MockHttpServletRequest(),
				new MockHttpServletResponse(),
				(peticion, respuesta) -> { });

		assertThat(MDC.get(InstitucionEnRegistroFiltro.CLAVE_MDC)).isNull();
	}

	@Test
	@DisplayName("aunque la petición falle, el MDC queda limpio para la siguiente")
	void aunqueLaPeticionFalleElMdcQuedaLimpio() {

		assertThatThrownBy(() -> filtro.doFilter(
				new MockHttpServletRequest(),
				new MockHttpServletResponse(),
				(peticion, respuesta) -> {
					throw new IllegalStateException("algo se rompió atendiendo la petición");
				}))
				.isInstanceOf(IllegalStateException.class);

		assertThat(MDC.get(InstitucionEnRegistroFiltro.CLAVE_MDC)).isNull();
	}

}
