package co.com.bookhive.gateway.domain.attempt;

import java.util.regex.Pattern;

public final class AttemptKeys {

	static final String PREFIX = "bookhive:";
	static final String GENERAL_ACTION = "general";

	private static final String COUNTER = "intentos:";
	private static final String BLOCK = "bloqueo:";
	private static final String ACCOUNT = "cuenta:";
	private static final String ORIGIN = "origen:";
	private static final String SEPARATOR = ":";
	private static final String REPLACEMENT = "_";
	private static final int MAX_PART_LENGTH = 128;
	private static final Pattern UNSAFE = Pattern.compile("[^A-Za-z0-9._:-]");

	private AttemptKeys() {
		super();
	}

	public static String accountCounter(final AttemptRequest request) {
		return PREFIX + COUNTER + ACCOUNT + safe(request.account()) + SEPARATOR + actionOf(request);
	}

	public static String accountBlock(final AttemptRequest request) {
		return PREFIX + BLOCK + ACCOUNT + safe(request.account()) + SEPARATOR + actionOf(request);
	}

	public static String originCounter(final AttemptRequest request) {
		return PREFIX + COUNTER + ORIGIN + safe(request.origin()) + SEPARATOR + actionOf(request);
	}

	public static String originBlock(final AttemptRequest request) {
		return PREFIX + BLOCK + ORIGIN + safe(request.origin()) + SEPARATOR + actionOf(request);
	}

	private static String actionOf(final AttemptRequest request) {
		return request.isSensitive() ? safe(request.action()) : GENERAL_ACTION;
	}

	static String safe(final String value) {
		final String cleaned = UNSAFE.matcher(value).replaceAll(REPLACEMENT);
		return cleaned.length() > MAX_PART_LENGTH ? cleaned.substring(0, MAX_PART_LENGTH) : cleaned;
	}

}
