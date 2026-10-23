CREATE TABLE notification (
    id                   UUID         NOT NULL,
    idempotency_key      VARCHAR(200) NOT NULL,
    kind                 VARCHAR(40)  NOT NULL,
    user_id              UUID         NOT NULL,
    channel              VARCHAR(20)  NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    provider_message_id  VARCHAR(200),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_notification                  PRIMARY KEY (id),
    CONSTRAINT uq_notification_idempotency_key  UNIQUE (idempotency_key),
    CONSTRAINT ck_notification_channel          CHECK (channel IN ('CORREO')),
    CONSTRAINT ck_notification_status           CHECK (status IN ('ENVIADO', 'SIN_CORREO'))
);

CREATE INDEX ix_notification_user ON notification (user_id);

GRANT INSERT ON notification TO inst_${flyway:defaultSchema};
