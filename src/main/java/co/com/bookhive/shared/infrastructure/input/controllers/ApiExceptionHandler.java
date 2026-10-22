package co.com.bookhive.shared.infrastructure.input.controllers;

import java.sql.SQLTransientConnectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.infrastructure.input.controllers.dto.ErrorResponseDTO;
import co.com.bookhive.shared.services.input.dto.request.ResolveMessageRequestDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private static final String INVALID_DATA_TECHNICAL_MESSAGE =
			"Se recibió una petición con datos inválidos: JSON malformado o parámetro con tipo incorrecto.";

	private static final String UNHANDLED_TECHNICAL_MESSAGE =
			"Se generó una excepción no controlada en el sistema. Por favor revisar los registros de errores.";

	private static final String BUSY_TECHNICAL_MESSAGE =
			"No hubo una conexión libre a la base de datos dentro del tiempo de espera; se rechaza la solicitud"
					+ " en lugar de dejarla esperando (ADR-005).";

	private final ResolveMessageInputPort messages;

	public ApiExceptionHandler(final ResolveMessageInputPort messages) {
		super();
		this.messages = messages;
	}

	@ExceptionHandler(BookHiveException.class)
	public ResponseEntity<ErrorResponseDTO> handle(final BookHiveException exception) {

		if (isDatabaseBusy(exception.getRootCause())) {
			return busy();
		}

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

		if (isDatabaseBusy(exception)) {
			return busy();
		}

		logger.error(UNHANDLED_TECHNICAL_MESSAGE, exception);

		return respond(HttpStatus.INTERNAL_SERVER_ERROR, MessageKey.REQUEST_UNHANDLED);
	}

	private ResponseEntity<ErrorResponseDTO> busy() {
		logger.warn(BUSY_TECHNICAL_MESSAGE);
		return respond(HttpStatus.SERVICE_UNAVAILABLE, MessageKey.REQUEST_BUSY);
	}

	private static boolean isDatabaseBusy(final Throwable exception) {

		Throwable current = exception;

		while (!ObjectHelper.isNull(current)) {
			if (current instanceof SQLTransientConnectionException) {
				return true;
			}
			current = current.getCause();
		}

		return false;
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
						messages.execute(new ResolveMessageRequestDTO(messageKey)).text(),
						TextHelper.getDefaultValue(MDC.get("traceId"))),
				status);
	}

}
