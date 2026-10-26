package co.com.bookhive.crosscutting.resilience;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class CircuitOpenException extends RuntimeException {

	private static final long serialVersionUID = 4021987156493285177L;

	public CircuitOpenException(final String service) {
		super("No se llamó a " + TextHelper.getDefaultValue(service)
				+ ": el corte temporal está abierto por fallos seguidos.");
	}

}
