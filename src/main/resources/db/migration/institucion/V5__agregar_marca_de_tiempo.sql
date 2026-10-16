-- Marca de tiempo de auditoria: cuando se creo cada fila.
--
-- Va como migracion NUEVA y no editando la V1 a la V4, aunque ahi se verian
-- mas limpias: una migracion ya aplicada no se vuelve a tocar. Flyway guarda la
-- suma de verificacion de cada archivo, y si se cambia el contenido de una que
-- alguien ya corrio, la siguiente ejecucion falla en su maquina.
--
-- TIMESTAMPTZ, no TIMESTAMP. La diferencia es justo lo de UTC-0: TIMESTAMPTZ
-- guarda el momento en UTC siempre, sin importar la zona de quien escribe.
-- TIMESTAMP guarda la pared del reloj y pierde la referencia.
--
-- DEFAULT now() a proposito: la hora la pone el motor, no la aplicacion. Hay
-- varias copias del backend corriendo en la nube y sus relojes pueden estar
-- corridos entre si; la base es un solo reloj. Y de paso la aplicacion no puede
-- mentir sobre cuando ocurrio algo, que es lo que le da valor a una auditoria.

ALTER TABLE prestamo
    ADD COLUMN creado_en TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE ejemplar
    ADD COLUMN creado_en TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE multa
    ADD COLUMN creado_en TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE parametros_institucion
    ADD COLUMN creado_en TIMESTAMPTZ NOT NULL DEFAULT now();

COMMENT ON COLUMN prestamo.creado_en IS
    'Momento exacto en UTC en que se registro el prestamo; lo pone el motor, no la aplicacion';
