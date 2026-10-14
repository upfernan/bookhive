package co.com.bookhive.inicializador.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.PrestarLibroCasoUso;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.impl.PrestarLibroCasoUsoImpl;
import co.com.bookhive.circulacion.servicios.input.adapters.PrestarLibroInteractor;
import co.com.bookhive.circulacion.servicios.input.ports.PrestarLibroInputPort;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.FechaInstitucionPort;
import co.com.bookhive.circulacion.servicios.output.ports.MultaRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.ParametrosInstitucionPort;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.UsuarioEnSesionPort;

// Aquí se le presentan a Spring las piezas del corazón que no tienen
// anotaciones: el caso de uso y el adaptador de entrada. Así Spring solo
// vive en el inicializador y en la infraestructura, y el corazón queda limpio.
//
// Spring entrega a cada método las implementaciones de los puertos que
// encuentra en la infraestructura.
@Configuration
public class CirculacionConfiguracion {

	@Bean
	public PrestarLibroCasoUso prestarLibroCasoUso(
			final ParametrosInstitucionPort parametrosInstitucion,
			final EjemplarRepositorioPort ejemplarRepositorio,
			final MultaRepositorioPort multaRepositorio,
			final PrestamoRepositorioPort prestamoRepositorio) {

		return new PrestarLibroCasoUsoImpl(
				parametrosInstitucion, ejemplarRepositorio, multaRepositorio, prestamoRepositorio);
	}

	@Bean
	public PrestarLibroInputPort prestarLibroInputPort(
			final PrestarLibroCasoUso prestarLibroCasoUso,
			final UsuarioEnSesionPort usuarioEnSesion,
			final FechaInstitucionPort fechaInstitucion) {

		return new PrestarLibroInteractor(prestarLibroCasoUso, usuarioEnSesion, fechaInstitucion);
	}

}
