package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.UUIDHelper;

public final class ValidadorInstitucion implements Validador {

	private static final Validador INSTANCIA = new ValidadorInstitucion();

	private ValidadorInstitucion() {
		super();
	}

	public static void ejecutarValidacion(final PrestarLibroDominio dominio) {
		INSTANCIA.validar(dominio);
	}

	@Override
	public void validar(final PrestarLibroDominio dominio) {

		if (UUIDHelper.esValorDefecto(dominio.getInstitucion())) {
			throw BookHiveException.crear(
					"No fue posible establecer la institución de quien atiende.",
					"La institución llegó vacía en PrestarLibroDominio; debe resolverse desde el token.");
		}
	}

}
