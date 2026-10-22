package co.com.bookhive.catalog.infrastructure.output.repositories.semistructured.typesense;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ScopedSearchKey {

	private static final String ALGORITHM = "HmacSHA256";
	private static final int PREFIX_LENGTH = 4;

	private ScopedSearchKey() {
		super();
	}

	public static String forInstitution(final String parentKey, final String institution) {
		return generate(parentKey, "{\"filter_by\":\"institution:=" + institution + "\"}");
	}

	public static String generate(final String parentKey, final String embeddedParameters) {
		final String digest = Base64.getEncoder().encodeToString(hmac(parentKey, embeddedParameters));
		final String rawKey = digest + parentKey.substring(0, PREFIX_LENGTH) + embeddedParameters;
		return Base64.getEncoder().encodeToString(rawKey.getBytes(StandardCharsets.UTF_8));
	}

	public static String derive(final String secret, final String label) {
		return HexFormat.of().formatHex(hmac(secret, label));
	}

	private static byte[] hmac(final String key, final String message) {
		try {
			final Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM));
			return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
		} catch (final GeneralSecurityException | IllegalArgumentException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.SEARCH_UNAVAILABLE,
					"No se pudo firmar la clave de búsqueda de Typesense.");
		}
	}

}
