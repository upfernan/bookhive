package co.com.bookhive.circulacion.servicios.input.adapters;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.PrestarLibroCasoUso;
import co.com.bookhive.circulacion.servicios.input.adapters.mapeador.PrestarLibroMapeador;
import co.com.bookhive.circulacion.servicios.input.dto.request.PrestarLibroRequestDTO;
import co.com.bookhive.circulacion.servicios.input.dto.response.PrestarLibroResponseDTO;
import co.com.bookhive.circulacion.servicios.input.ports.PrestarLibroInputPort;
import co.com.bookhive.circulacion.servicios.output.ports.FechaInstitucionPort;
import co.com.bookhive.circulacion.servicios.output.ports.UsuarioEnSesionPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.ObjectHelper;

// Adaptador de entrada (Interactor) de "prestar libro".
//
// Implementa el puerto de entrada que usa el controlador. Recibe el DTO,
// lo convierte en dominio, llama al contrato del caso de uso (nunca a la
// implementación) y devuelve el DTO de respuesta. No tiene reglas de negocio
// ni anotaciones de Spring: se conecta desde el inicializador.
public final class PrestarLibroInteractor implements PrestarLibroInputPort {

	private final PrestarLibroCasoUso casoUso;
	private final UsuarioEnSesionPort usuarioEnSesion;
	private final FechaInstitucionPort fechaInstitucion;

	public PrestarLibroInteractor(
			final PrestarLibroCasoUso casoUso,
			final UsuarioEnSesionPort usuarioEnSesion,
			final FechaInstitucionPort fechaInstitucion) {

		super();
		this.casoUso = exigirDependencia(casoUso, "el caso de uso");
		this.usuarioEnSesion = exigirDependencia(usuarioEnSesion, "el usuario en sesión");
		this.fechaInstitucion = exigirDependencia(fechaInstitucion, "la fecha de la institución");
	}

	@Override
	public PrestarLibroResponseDTO ejecutar(final PrestarLibroRequestDTO peticion) {

		final PrestarLibroDominio dominio = PrestarLibroMapeador.aDominio(
				peticion,
				usuarioEnSesion.obtenerIdentificador(),
				usuarioEnSesion.obtenerInstitucion(),
				fechaInstitucion.obtenerFechaActual());

		final PrestarLibroDominio prestamo = casoUso.ejecutar(dominio);

		return PrestarLibroMapeador.aRespuesta(prestamo);
	}

	private static <T> T exigirDependencia(final T dependencia, final String nombre) {
		if (ObjectHelper.esNulo(dependencia)) {
			throw BookHiveException.crear(
					"Se presentó un problema inesperado al registrar el préstamo.",
					"El adaptador de entrada PrestarLibro se creó sin " + nombre + ".");
		}
		return dependencia;
	}

}
