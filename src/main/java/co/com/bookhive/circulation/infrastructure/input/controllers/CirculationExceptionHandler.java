package co.com.bookhive.circulation.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import co.com.bookhive.circulation.infrastructure.input.controllers.dto.ErrorResponseDTO;
import co.com.bookhive.circulation.services.output.ports.MessageCatalogPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@RestControllerAdvice
public class CirculationExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(CirculationExceptionHandler.class);

	private static final String INVALID_DATA_TECHNICAL_MESSAGE =
			"Se recibió una petición con datos inválidos: JSON malformado o parámetro con tipo incorrecto.";

	private static final String UNHANDLED_TECHNICAL_MESSAGE =
			"Se generó una excepción no controlada en el sistema. Por favor revisar los registros de errores.";

	private final MessageCatalogPort messageCatalog;

	public CirculationExceptionHandler(final MessageCatalogPort messageCatalog) {
		super();
		this.messageCatalog = messageCatalog;
	}

	@ExceptionHandler(BookHiveException.class)
	public ResponseEntity<ErrorResponseDTO> handle(final BookHiveException exception) {

		if (BookHiveException.Kind.UNEXPECTED.equals(exception.getKind())) {
			logger.error("Error inesperado: {}", exception.getTechnicalMessage(), exception.getRootCause());
		} else {
			logger.warn("Solicitud rechazada: {}", exception.getTechnicalMessage());
		}

		return respond(statusOf(exception), exception.getUserMessageKey());
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorResponseDTO> handleInvalidData(final Exception exception) {

		logger.warn("{} Tipo: {}", INVALID_DATA_TECHNICAL_MESSAGE, exception.getClass().getSimpleName());

		return respond(HttpStatus.BAD_REQUEST, MessageKey.REQUEST_INVALID_DATA);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handle(final Exception exception) {

		logger.error(UNHANDLED_TECHNICAL_MESSAGE, exception);

		return respond(HttpStatus.INTERNAL_SERVER_ERROR, MessageKey.REQUEST_UNHANDLED);
	}

	private static HttpStatus statusOf(final BookHiveException exception) {

		return switch (exception.getKind()) {
			case MISSING_DATA -> HttpStatus.BAD_REQUEST;
			case STATE_DOES_NOT_ALLOW_IT -> HttpStatus.CONFLICT;
			case NOT_ALLOWED -> HttpStatus.FORBIDDEN;
			case UNEXPECTED -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}

	private ResponseEntity<ErrorResponseDTO> respond(
			final HttpStatus status, final String messageKey) {

		return new ResponseEntity<>(
				ErrorResponseDTO.create(
						messageCatalog.textOf(messageKey),
						TextHelper.getDefaultValue(MDC.get("traceId"))),
				status);
	}

}
