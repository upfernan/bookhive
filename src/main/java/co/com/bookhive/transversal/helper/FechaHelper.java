package co.com.bookhive.transversal.helper;

import java.time.LocalDate;

public final class FechaHelper {

	private static final LocalDate VALOR_DEFECTO = LocalDate.of(1900, 1, 1);

	private FechaHelper() {
		super();
	}

	public static LocalDate obtenerValorDefecto() {
		return VALOR_DEFECTO;
	}

	public static LocalDate obtenerValorDefecto(final LocalDate valor) {
		return ObjectHelper.obtenerValorDefecto(valor, VALOR_DEFECTO);
	}

	public static boolean esValorDefecto(final LocalDate valor) {
		return VALOR_DEFECTO.equals(obtenerValorDefecto(valor));
	}

}
