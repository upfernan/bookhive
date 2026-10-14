package co.com.bookhive.inicializador.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.PrestarLibroCasoUso;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.impl.PrestarLibroCasoUsoImpl;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.MultaRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.ParametrosInstitucionPort;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;

// Aquí se arman las piezas del negocio. Existe para que el caso de uso pueda
// quedarse sin una sola anotación de Spring: el corazón no conoce el framework,
// y es el inicializador quien lo construye y le entrega sus puertos.
//
// Si mañana se cambia de framework, se reescribe esta clase y el negocio no se
// entera.
@Configuration
public class CirculacionConfiguracion {

	@Bean
	PrestarLibroCasoUso prestarLibroCasoUso(
			final ParametrosInstitucionPort parametrosInstitucion,
			final EjemplarRepositorioPort ejemplarRepositorio,
			final MultaRepositorioPort multaRepositorio,
			final PrestamoRepositorioPort prestamoRepositorio) {

		return new PrestarLibroCasoUsoImpl(
				parametrosInstitucion,
				ejemplarRepositorio,
				multaRepositorio,
				prestamoRepositorio);
	}

}
