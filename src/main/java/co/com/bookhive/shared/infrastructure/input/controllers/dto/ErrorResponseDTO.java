package co.com.bookhive.shared.infrastructure.input.controllers.dto;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class ErrorResponseDTO {

	private String message;
	private String traceId;

	public ErrorResponseDTO() {
		super();
		setMessage(TextHelper.getDefaultValue());
		setTraceId(TextHelper.getDefaultValue());
	}

	private ErrorResponseDTO(final String message, final String traceId) {
		super();
		setMessage(message);
		setTraceId(traceId);
	}

	public static ErrorResponseDTO create(final String message, final String traceId) {
		return new ErrorResponseDTO(message, traceId);
	}

	public String getMessage() {
		return message;
	}

	public String getTraceId() {
		return traceId;
	}

	private void setMessage(final String message) {
		this.message = TextHelper.getDefaultValue(message);
	}

	private void setTraceId(final String traceId) {
		this.traceId = TextHelper.getDefaultValue(traceId);
	}

}
