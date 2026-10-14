package co.com.bookhive.transversal.helper;

import java.util.UUID;

public final class UUIDHelper {

	private static final UUID VALOR_DEFECTO = UUID.fromString("00000000-0000-0000-0000-000000000000");

	private UUIDHelper() {
		super();
	}

	public static UUID obtenerValorDefecto() {
		return VALOR_DEFECTO;
	}

	public static UUID generarNuevo() {
		return UUID.randomUUID();
	}

	public static UUID obtenerValorDefecto(final UUID valor) {
		return ObjectHelper.obtenerValorDefecto(valor, VALOR_DEFECTO);
	}

	// Un valor nulo cuenta como valor por defecto. Se dice de forma explícita
	// para que el analizador sepa que, si esto da falso, el valor no es nulo.
	public static boolean esValorDefecto(final UUID valor) {
		return ObjectHelper.esNulo(valor) || VALOR_DEFECTO.equals(valor);
	}

}
