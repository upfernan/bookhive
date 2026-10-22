package co.com.bookhive.access.services.output.ports.repositories.entities;

import co.com.bookhive.crosscutting.helper.TextHelper;

public class PermissionEntity {

	private String code;

	public PermissionEntity() {
		super();
		setCode(TextHelper.getDefaultValue());
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = TextHelper.getDefaultValue(code).trim();
	}

}
