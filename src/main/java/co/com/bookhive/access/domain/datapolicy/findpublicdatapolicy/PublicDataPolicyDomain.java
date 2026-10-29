package co.com.bookhive.access.domain.datapolicy.findpublicdatapolicy;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record PublicDataPolicyDomain(int version, String content, Instant publishedAt) {

	private static final Pattern LABEL = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?");
	private static final int MAX_DOMAIN = 253;
	private static final int MIN_LABELS = 2;

	public PublicDataPolicyDomain {
		content = TextHelper.getDefaultValue(content);
	}

	public static String normalizedDomain(final String domain) {
		final String normalized = TextHelper.getDefaultValue(domain).trim().toLowerCase(Locale.ROOT);
		if (!isValid(normalized)) {
			throw notFound("Se pidió el aviso de datos de un dominio inválido.");
		}
		return normalized;
	}

	private static boolean isValid(final String domain) {
		final String[] labels = domain.split("\\.", -1);
		return domain.length() <= MAX_DOMAIN && labels.length >= MIN_LABELS
				&& Arrays.stream(labels).allMatch(label -> LABEL.matcher(label).matches());
	}

	public static BookHiveException notFound(final String technicalMessage) {
		return BookHiveException.createNotFound(MessageKey.DATA_POLICY_PUBLIC_NOT_FOUND, technicalMessage);
	}

}
