package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class UserEntity {

	public static final String STATUS_ACTIVE = "ACTIVO";
	public static final String STATUS_SUSPENDED = "SUSPENDIDO";
	public static final String STATUS_RETIRED = "RETIRADO";

	private UUID id;
	private String name;
	private String email;
	private String status;

	private Instant createdAt;

	public UserEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setName("");
		setEmail("");
		setStatus(STATUS_ACTIVE);
		setCreatedAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = ObjectHelper.getDefaultValue(name, "").trim();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = ObjectHelper.getDefaultValue(email, "").trim();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = ObjectHelper.isNull(status) || status.isBlank() ? STATUS_ACTIVE : status.trim();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
