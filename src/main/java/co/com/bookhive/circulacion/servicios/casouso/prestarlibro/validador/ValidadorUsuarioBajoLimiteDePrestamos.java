package co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador;

import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;

public final class ValidadorUsuarioBajoLimiteDePrestamos {

	private static final int LIMITE_DE_PRESTAMOS_SIMULTANEOS = 3;

	private ValidadorUsuarioBajoLimiteDePrestamos() {
		super();
	}

	public static void ejecutarValidacion(
			final PrestarLibroDominio dominio,
			final PrestamoRepositorioPort prestamoRepositorio) {

		final UUID usuario = dominio.getUsuario();
		final int activos = prestamoRepositorio.contarActivosPorUsuario(usuario);

		if (activos >= LIMITE_DE_PRESTAMOS_SIMULTANEOS) {
			throw BookHiveException.crear(
					"El usuario ya alcanzó el máximo de ejemplares que puede tener prestados al tiempo.",
					"El usuario " + usuario + " tiene " + activos
							+ " préstamos activos y el límite es " + LIMITE_DE_PRESTAMOS_SIMULTANEOS + ".");
		}
	}

}
