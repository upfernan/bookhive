CREATE TABLE reservation (
    id               UUID         NOT NULL,
    book_id          UUID         NOT NULL,
    user_id          UUID         NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    requested_at     TIMESTAMPTZ  NOT NULL DEFAULT clock_timestamp(),
    book_copy_id     UUID,
    assigned_at      TIMESTAMPTZ,
    pickup_deadline  DATE,

    CONSTRAINT pk_reservation            PRIMARY KEY (id),
    CONSTRAINT fk_reservation_book       FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_reservation_user       FOREIGN KEY (user_id) REFERENCES library_user (id),
    CONSTRAINT fk_reservation_book_copy  FOREIGN KEY (book_copy_id) REFERENCES book_copy (id),
    CONSTRAINT ck_reservation_status     CHECK (status IN ('EN_ESPERA', 'ASIGNADA', 'RETIRADA', 'VENCIDA', 'CANCELADA'))
);

CREATE UNIQUE INDEX uq_reservation_active_per_user_book
    ON reservation (user_id, book_id)
    WHERE status IN ('EN_ESPERA', 'ASIGNADA');

CREATE UNIQUE INDEX uq_reservation_assigned_copy
    ON reservation (book_copy_id)
    WHERE status = 'ASIGNADA';

CREATE INDEX ix_reservation_waiting_queue
    ON reservation (book_id, requested_at, id)
    WHERE status = 'EN_ESPERA';

CREATE INDEX ix_reservation_user ON reservation (user_id);

GRANT INSERT, UPDATE ON reservation TO inst_${flyway:defaultSchema};

REVOKE DELETE, TRUNCATE ON reservation FROM inst_${flyway:defaultSchema};

INSERT INTO permission (code, description) VALUES
    ('reservation.create', 'Reservar libros y gestionar las reservas propias');
