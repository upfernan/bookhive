package co.com.bookhive.notification.infrastructure.output.repositories.structured.jpa.entities;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification")
public class NotificationJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "idempotency_key", updatable = false)
	private String idempotencyKey;

	@Column(name = "kind", updatable = false)
	private String kind;

	@Column(name = "user_id", updatable = false)
	private UUID user;

	@Column(name = "channel", updatable = false)
	private String channel;

	@Column(name = "status", updatable = false)
	private String status;

	@Column(name = "provider_message_id", updatable = false)
	private String providerMessageId;

	public NotificationJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setIdempotencyKey(TextHelper.getDefaultValue());
		setKind(TextHelper.getDefaultValue());
		setUser(UUIDHelper.getDefaultValue());
		setChannel(TextHelper.getDefaultValue());
		setStatus(TextHelper.getDefaultValue());
		setProviderMessageId(TextHelper.getDefaultValue());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(final String idempotencyKey) {
		this.idempotencyKey = TextHelper.getDefaultValue(idempotencyKey);
	}

	public String getKind() {
		return kind;
	}

	public void setKind(final String kind) {
		this.kind = TextHelper.getDefaultValue(kind);
	}

	public UUID getUser() {
		return user;
	}

	public void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(final String channel) {
		this.channel = TextHelper.getDefaultValue(channel);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = TextHelper.getDefaultValue(status);
	}

	public String getProviderMessageId() {
		return providerMessageId;
	}

	public void setProviderMessageId(final String providerMessageId) {
		this.providerMessageId = TextHelper.getDefaultValue(providerMessageId);
	}

}
