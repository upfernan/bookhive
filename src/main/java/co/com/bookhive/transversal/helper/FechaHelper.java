package co.com.bookhive.transversal.helper;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

// REGLA: todas las fechas de BookHive se manejan en UTC-0.
// Nunca usar LocalDate.now() sin zona: ese método lee la zona horaria de la
// máquina, así que el mismo código da días distintos en el portátil (UTC-5) y
// en el servidor (UTC). Para obtener el día actual se usa hoyEnUtc().
public final class FechaHelper {

	private static final LocalDate VALOR_DEFECTO = LocalDate.of(1900, 1, 1);

	private static final Clock RELOJ_UTC = Clock.system(ZoneOffset.UTC);

	private FechaHelper() {
		super();
	}

	public static LocalDate obtenerValorDefecto() {
		return VALOR_DEFECTO;
	}

	public static LocalDate hoyEnUtc() {
		return hoyEnUtc(RELOJ_UTC);
	}

	// Variante con reloj para poder congelar el tiempo en las pruebas.
	// Toma solo el instante del reloj y lo lleva a UTC: la zona del reloj se
	// ignora a propósito, para que el resultado sea UTC venga de donde venga.
	public static LocalDate hoyEnUtc(final Clock reloj) {
		return LocalDate.ofInstant(
				ObjectHelper.obtenerValorDefecto(reloj, RELOJ_UTC).instant(),
				ZoneOffset.UTC);
	}

	public static LocalDate obtenerValorDefecto(final LocalDate valor) {
		return ObjectHelper.obtenerValorDefecto(valor, VALOR_DEFECTO);
	}

	// Un valor nulo cuenta como valor por defecto. Se dice de forma explícita
	// para que el analizador sepa que, si esto da falso, el valor no es nulo.
	public static boolean esValorDefecto(final LocalDate valor) {
		return ObjectHelper.esNulo(valor) || VALOR_DEFECTO.equals(valor);
	}

}
