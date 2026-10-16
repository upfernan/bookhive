CREATE TABLE mensaje (
    codigo  VARCHAR(100) NOT NULL,
    texto   TEXT         NOT NULL,

    CONSTRAINT pk_mensaje        PRIMARY KEY (codigo),
    CONSTRAINT ck_mensaje_texto  CHECK (LENGTH(TRIM(texto)) > 0)
);

COMMENT ON TABLE mensaje IS
    'Catalogo de mensajes (ADR-018). Unico para todas las instituciones: por eso vive en el esquema compartido y no en el de cada una';

COMMENT ON COLUMN mensaje.texto IS
    'Admite datos variables con la forma {0}, {1}: el nombre de la institucion o una fecha se insertan al mostrarlo';

INSERT INTO mensaje (codigo, texto) VALUES
    ('loan.data.required', 'No llegaron los datos del préstamo.'),
    ('loan.book-copy.required', 'Debe indicar cuál ejemplar se va a prestar.'),
    ('loan.user.required', 'Debe indicar a cuál usuario se le presta el ejemplar.'),
    ('loan.librarian.required', 'No fue posible establecer quién está registrando el préstamo.'),
    ('loan.institution.required', 'No fue posible establecer la institución de quien atiende.'),
    ('loan.date.required', 'No fue posible establecer la fecha del préstamo.'),
    ('loan.date.in-the-future', 'No se puede registrar un préstamo con fecha futura.'),
    ('loan.not-confirmed', 'No fue posible confirmar el préstamo.'),
    ('loan.unexpected', 'Se presentó un problema inesperado al registrar el préstamo.'),
    ('book-copy.not-found', 'El ejemplar indicado no existe en el catálogo.'),
    ('book-copy.not-available', 'El ejemplar no está disponible: ya está prestado o reservado.'),
    ('book-copy.reference-only', 'Este ejemplar solo puede consultarse en la sala; no se presta a domicilio.'),
    ('user.with-pending-fines', 'El usuario tiene multas pendientes y no puede llevar más ejemplares.'),
    ('user.at-loan-limit', 'El usuario ya alcanzó el máximo de ejemplares que puede tener prestados al tiempo.'),
    ('institution.not-active', 'La institución no está activa en BookHive.'),
    ('institution.without-parameters', 'La institución todavía no tiene configuradas sus reglas de préstamo.'),
    ('session.not-resolved', 'No fue posible establecer quién está atendiendo el préstamo.'),
    ('request.unexpected', 'Se presentó un problema inesperado al atender la solicitud.'),
    ('request.invalid-data', 'La petición contiene datos inválidos. Por favor verifique el formato e inténtelo de nuevo.'),
    ('request.unhandled', 'Ocurrió un error inesperado. Por favor inténtelo de nuevo y si el problema persiste contacte a un administrador.');
