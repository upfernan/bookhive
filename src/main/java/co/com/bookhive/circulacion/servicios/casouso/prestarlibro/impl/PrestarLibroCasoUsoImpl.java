package co.com.bookhive.circulacion.servicios.casouso.prestarlibro.impl;

import java.time.LocalDate;

import co.com.bookhive.circulacion.dominio.prestarlibro.PoliticaPrestamo;
import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.dominio.prestarlibro.validador.ValidadorBibliotecario;
import co.com.bookhive.circulacion.dominio.prestarlibro.validador.ValidadorEjemplar;
import co.com.bookhive.circulacion.dominio.prestarlibro.validador.ValidadorFechaPrestamo;
import co.com.bookhive.circulacion.dominio.prestarlibro.validador.ValidadorInstitucion;
import co.com.bookhive.circulacion.dominio.prestarlibro.validador.ValidadorUsuario;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.PrestarLibroCasoUso;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador.ValidadorEjemplarDisponible;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador.ValidadorEjemplarPrestable;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador.ValidadorUsuarioBajoLimiteDePrestamos;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador.ValidadorUsuarioSinMultasPendientes;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.MultaRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.ParametrosInstitucionPort;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.ObjectHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;

// Implementación del caso de uso "prestar libro".
//
// No lleva ninguna anotación de Spring: el corazón no depende de ningún
// framework. Recibe los puertos de salida por el constructor y solo habla con
// ellos; nunca sabe si detrás hay una base de datos u otra cosa. Trabaja
// 100 % con dominio: no conoce DTO ni entidades.
public final class PrestarLibroCasoUsoImpl implements PrestarLibroCasoUso {

	private final ParametrosInstitucionPort parametrosInstitucion;
	private final EjemplarRepositorioPort ejemplarRepositorio;
	private final MultaRepositorioPort multaRepositorio;
	private final PrestamoRepositorioPort prestamoRepositorio;

	public PrestarLibroCasoUsoImpl(
			final ParametrosInstitucionPort parametrosInstitucion,
			final EjemplarRepositorioPort ejemplarRepositorio,
			final MultaRepositorioPort multaRepositorio,
			final PrestamoRepositorioPort prestamoRepositorio) {

		super();
		this.parametrosInstitucion = exigirDependencia(parametrosInstitucion, "los parámetros de la institución");
		this.ejemplarRepositorio = exigirDependencia(ejemplarRepositorio, "el repositorio de ejemplares");
		this.multaRepositorio = exigirDependencia(multaRepositorio, "el repositorio de multas");
		this.prestamoRepositorio = exigirDependencia(prestamoRepositorio, "el repositorio de préstamos");
	}

	@Override
	public PrestarLibroDominio ejecutar(final PrestarLibroDominio dominio) {

		final PrestarLibroDominio prestamo = exigirDatosDelPrestamo(dominio);

		// 1. Que lleguen todos los datos: ejemplar, usuario, bibliotecario,
		// institución y fecha.
		validarDatosCompletos(prestamo);

		// 2. Las reglas de la institución en curso (RN-10, ADR-017).
		final int diasDePrestamo = parametrosInstitucion.obtenerDiasDePrestamo();
		final int maximoPrestamosSimultaneos = parametrosInstitucion.obtenerMaximoPrestamosSimultaneos();

		// 3. Que el ejemplar exista, esté libre y se pueda llevar a casa.
		validarEjemplar(prestamo);

		// 4. Que el usuario no tenga multas ni haya llegado al máximo.
		validarUsuario(prestamo, maximoPrestamosSimultaneos);

		// 5. La fecha de entrega, con el plazo de la institución.
		final LocalDate fechaVencimiento = PoliticaPrestamo.calcularFechaVencimiento(
				prestamo.getFechaPrestamo(), diasDePrestamo);

		// 6. El préstamo nace con su propio identificador y se guarda.
		final PrestarLibroDominio prestamoRegistrado = new PrestarLibroDominio(
				UUIDHelper.generarNuevo(),
				prestamo.getEjemplar(),
				prestamo.getUsuario(),
				prestamo.getInstitucion(),
				prestamo.getBibliotecario(),
				prestamo.getFechaPrestamo(),
				fechaVencimiento);

		prestamoRepositorio.guardar(prestamoRegistrado);

		return prestamoRegistrado;
	}

	private static void validarDatosCompletos(final PrestarLibroDominio prestamo) {
		ValidadorEjemplar.ejecutarValidacion(prestamo);
		ValidadorUsuario.ejecutarValidacion(prestamo);
		ValidadorBibliotecario.ejecutarValidacion(prestamo);
		ValidadorInstitucion.ejecutarValidacion(prestamo);
		ValidadorFechaPrestamo.ejecutarValidacion(prestamo);
	}

	private void validarEjemplar(final PrestarLibroDominio prestamo) {
		ValidadorEjemplarDisponible.ejecutarValidacion(prestamo, ejemplarRepositorio);
		ValidadorEjemplarPrestable.ejecutarValidacion(prestamo, ejemplarRepositorio);
	}

	private void validarUsuario(final PrestarLibroDominio prestamo, final int maximoPrestamosSimultaneos) {
		ValidadorUsuarioSinMultasPendientes.ejecutarValidacion(prestamo, multaRepositorio);
		ValidadorUsuarioBajoLimiteDePrestamos.ejecutarValidacion(
				prestamo, prestamoRepositorio, maximoPrestamosSimultaneos);
	}

	private static PrestarLibroDominio exigirDatosDelPrestamo(final PrestarLibroDominio dominio) {
		if (ObjectHelper.esNulo(dominio)) {
			throw BookHiveException.crear(
					"No llegaron los datos del préstamo.",
					"El caso de uso PrestarLibro recibió un dominio nulo.");
		}
		return dominio;
	}

	private static <T> T exigirDependencia(final T dependencia, final String nombre) {
		if (ObjectHelper.esNulo(dependencia)) {
			throw BookHiveException.crear(
					"Se presentó un problema inesperado al registrar el préstamo.",
					"El caso de uso PrestarLibro se creó sin " + nombre + ".");
		}
		return dependencia;
	}

}
