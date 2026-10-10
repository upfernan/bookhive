package co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador;

import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;

public final class ValidadorUsuarioBajoLimiteDePrestamos {

	private ValidadorUsuarioBajoLimiteDePrestamos() {
		super();
	}

	public static void ejecutarValidacion(
			final PrestarLibroDominio dominio,
			final PrestamoRepositorioPort prestamoRepositorio,
			final int maximoPrestamosSimultaneos) {

		if (maximoPrestamosSimultaneos <= 0) {
			throw BookHiveException.crear(
					"La institución no tiene configurado el máximo de préstamos por usuario.",
					"El máximo recibido fue " + maximoPrestamosSimultaneos + "; debe ser mayor que cero.");
		}

		final UUID usuario = dominio.getUsuario();
		final int activos = prestamoRepositorio.contarActivosPorUsuario(usuario);

		if (activos >= maximoPrestamosSimultaneos) {
			throw BookHiveException.crear(
					"El usuario ya alcanzó el máximo de ejemplares que puede tener prestados al tiempo.",
					"El usuario " + usuario + " tiene " + activos
							+ " préstamos activos y el máximo de la institución es "
							+ maximoPrestamosSimultaneos + ".");
		}
	}

}
