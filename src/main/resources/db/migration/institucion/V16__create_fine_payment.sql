CREATE TABLE fine_settlement (
    id                 UUID          NOT NULL,
    fine_id            UUID          NOT NULL,
    payment_reference  VARCHAR(100)  NOT NULL,
    amount             NUMERIC(12,2) NOT NULL,
    settled_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT pk_fine_settlement         PRIMARY KEY (id),
    CONSTRAINT uq_fine_settlement_fine    UNIQUE (fine_id),
    CONSTRAINT fk_fine_settlement_fine    FOREIGN KEY (fine_id) REFERENCES fine (id),
    CONSTRAINT ck_fine_settlement_amount  CHECK (amount > 0)
);

CREATE TABLE fine_payment (
    id               UUID         NOT NULL,
    reference        VARCHAR(100) NOT NULL,
    fine_id          UUID         NOT NULL,
    payer_id         UUID         NOT NULL,
    institution_id   UUID         NOT NULL,
    amount_in_cents  BIGINT       NOT NULL,
    currency         CHAR(3)      NOT NULL,
    expires_at       TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_fine_payment            PRIMARY KEY (id),
    CONSTRAINT uq_fine_payment_reference  UNIQUE (reference),
    CONSTRAINT fk_fine_payment_fine       FOREIGN KEY (fine_id) REFERENCES fine (id),
    CONSTRAINT ck_fine_payment_amount     CHECK (amount_in_cents > 0),
    CONSTRAINT ck_fine_payment_currency   CHECK (currency = 'COP')
);

CREATE INDEX ix_fine_payment_fine ON fine_payment (fine_id, payer_id, expires_at);
CREATE INDEX ix_fine_payment_created ON fine_payment (created_at);

CREATE TABLE fine_payment_event (
    id               UUID         NOT NULL,
    payment_id       UUID         NOT NULL,
    transaction_id   VARCHAR(64)  NOT NULL,
    status           VARCHAR(16)  NOT NULL,
    amount_in_cents  BIGINT       NOT NULL,
    source           VARCHAR(16)  NOT NULL,
    received_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_fine_payment_event              PRIMARY KEY (id),
    CONSTRAINT uq_fine_payment_event_transaction  UNIQUE (transaction_id, status),
    CONSTRAINT fk_fine_payment_event_payment      FOREIGN KEY (payment_id) REFERENCES fine_payment (id),
    CONSTRAINT ck_fine_payment_event_status       CHECK (status IN ('PENDING', 'APPROVED', 'DECLINED', 'VOIDED', 'ERROR')),
    CONSTRAINT ck_fine_payment_event_source       CHECK (source IN ('EVENTO', 'CONSULTA', 'CONCILIACION')),
    CONSTRAINT ck_fine_payment_event_amount       CHECK (amount_in_cents >= 0)
);

CREATE INDEX ix_fine_payment_event_payment ON fine_payment_event (payment_id, status);

GRANT INSERT ON fine_settlement, fine_payment, fine_payment_event TO inst_${flyway:defaultSchema};

REVOKE UPDATE, DELETE, TRUNCATE ON fine_settlement, fine_payment, fine_payment_event FROM inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('fine.pay', 'Pagar en línea las multas propias');
