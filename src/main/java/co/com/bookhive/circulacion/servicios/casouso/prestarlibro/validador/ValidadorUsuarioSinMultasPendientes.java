package co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador;

import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.output.ports.MultaRepositorioPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;

public final class ValidadorUsuarioSinMultasPendientes {

	private ValidadorUsuarioSinMultasPendientes() {
		super();
	}

	public static void ejecutarValidacion(
			final PrestarLibroDominio dominio,
			final MultaRepositorioPort multaRepositorio) {

		final UUID usuario = dominio.getUsuario();

		if (multaRepositorio.tienePendientes(usuario)) {
			throw BookHiveException.crear(
					"El usuario tiene multas pendientes y no puede llevar más ejemplares.",
					"El usuario " + usuario + " tiene al menos una multa sin pagar.");
		}
	}

}
