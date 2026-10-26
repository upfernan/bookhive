package co.com.bookhive.notification.domain.notification.sendnotification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record SendNotificationDomain(String kind, UUID user, UUID book, UUID loan, LocalDate dueDate) {

	public static final String KIND_LOAN_REGISTERED = "PRESTAMO_REGISTRADO";
	public static final String KIND_LOAN_DUE_SOON = "PRESTAMO_POR_VENCER";
	public static final String KIND_FINE_GENERATED = "MULTA_GENERADA";
	public static final String KIND_FINE_PAID = "MULTA_PAGADA";
	public static final String KIND_RESERVATION_READY = "RESERVA_LISTA";

	private static final Map<String, List<String>> TEMPLATES = Map.of(
			KIND_LOAN_REGISTERED, List.of(MessageKey.EMAIL_LOAN_REGISTERED_SUBJECT, MessageKey.EMAIL_LOAN_REGISTERED_BODY),
			KIND_LOAN_DUE_SOON, List.of(MessageKey.EMAIL_LOAN_DUE_SOON_SUBJECT, MessageKey.EMAIL_LOAN_DUE_SOON_BODY),
			KIND_FINE_GENERATED, List.of(MessageKey.EMAIL_FINE_GENERATED_SUBJECT, MessageKey.EMAIL_FINE_GENERATED_BODY),
			KIND_FINE_PAID, List.of(MessageKey.EMAIL_FINE_PAID_SUBJECT, MessageKey.EMAIL_FINE_PAID_BODY),
			KIND_RESERVATION_READY, List.of(
					MessageKey.EMAIL_RESERVATION_READY_SUBJECT, MessageKey.EMAIL_RESERVATION_READY_BODY));
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public SendNotificationDomain {
		kind = TextHelper.getDefaultValue(kind);
		user = UUIDHelper.getDefaultValue(user);
		book = UUIDHelper.getDefaultValue(book);
		loan = UUIDHelper.getDefaultValue(loan);
		dueDate = DateHelper.getDefaultValue(dueDate);
	}

	public void validate() {
		if (!TEMPLATES.containsKey(kind)) {
			throw invalid("El aviso de tipo \"" + kind + "\" no se sabe enviar.");
		}
		if (UUIDHelper.isDefaultValue(user) || UUIDHelper.isDefaultValue(book) || UUIDHelper.isDefaultValue(loan)) {
			throw invalid("El aviso " + kind + " llegó sin usuario, libro o préstamo.");
		}
		if (DateHelper.isDefaultValue(dueDate)) {
			throw invalid("El aviso " + kind + " llegó sin fecha de vencimiento.");
		}
	}

	public String subjectKey() {
		return TEMPLATES.getOrDefault(kind, List.of("", "")).get(0);
	}

	public String bodyKey() {
		return TEMPLATES.getOrDefault(kind, List.of("", "")).get(1);
	}

	public String idempotencyKey() {
		return kind + ":" + loan + ":" + dueDate;
	}

	public String fill(final String template, final String name, final String title) {
		return TextHelper.getDefaultValue(template)
				.replace("{nombre}", TextHelper.isDefaultValue(name) ? "lector" : name)
				.replace("{titulo}", TextHelper.isDefaultValue(title) ? "que tienes prestado" : title)
				.replace("{fecha}", dueDate.format(DATE));
	}

	private static BookHiveException invalid(final String technicalMessage) {
		return BookHiveException.create(MessageKey.NOTIFICATION_DATA_INVALID, technicalMessage);
	}

}
