package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "library_user")
public class UserJpaEntity {

	public static final String STATUS_ACTIVE = "ACTIVO";
	public static final String STATUS_SUSPENDED = "SUSPENDIDO";
	public static final String STATUS_RETIRED = "RETIRADO";
	public static final String STATUS_PURGED = "DEPURADO";

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;

	@Column(name = "status")
	private String status;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	public UserJpaEntity() {
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
		this.status = ObjectHelper.getDefaultValue(status, STATUS_ACTIVE).trim();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
