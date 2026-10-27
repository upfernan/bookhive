package co.com.bookhive.access.domain.datapolicy.findpublicdatapolicy;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record PublicDataPolicyDomain(int version, String content, Instant publishedAt) {

	private static final Pattern DOMAIN = Pattern.compile("^(?=.{3,253}$)[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+$");

	public PublicDataPolicyDomain {
		content = TextHelper.getDefaultValue(content);
	}

	public static String normalizedDomain(final String domain) {
		final String normalized = TextHelper.getDefaultValue(domain).trim().toLowerCase(Locale.ROOT);
		if (!DOMAIN.matcher(normalized).matches()) {
			throw notFound("Se pidió el aviso de datos de un dominio inválido.");
		}
		return normalized;
	}

	public static BookHiveException notFound(final String technicalMessage) {
		return BookHiveException.createNotFound(MessageKey.DATA_POLICY_PUBLIC_NOT_FOUND, technicalMessage);
	}

}
