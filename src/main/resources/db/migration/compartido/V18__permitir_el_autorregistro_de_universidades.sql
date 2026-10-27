ALTER TABLE institucion ADD COLUMN alta VARCHAR(20) NOT NULL DEFAULT 'MANUAL';

ALTER TABLE institucion ADD CONSTRAINT ck_institucion_alta CHECK (alta IN ('MANUAL', 'AUTOREGISTRO'));

INSERT INTO mensaje (codigo, texto) VALUES
    ('registration.institution.unavailable', 'En este momento no podemos crear la biblioteca de su institución. Intente de nuevo en unos minutos.'),
    ('data-policy.default', 'La institución es la responsable del tratamiento de sus datos personales dentro de BookHive, conforme a la Ley 1581 de 2012 y el Decreto 1377 de 2013.

Datos que se tratan: su nombre, su correo institucional y el registro de sus préstamos, devoluciones, reservas, multas y pagos.

Para qué se usan: prestarle material de la biblioteca, llevar la fila de reservas, calcular y cobrar las multas por devolución tardía, y enviarle avisos sobre sus préstamos, reservas y pagos. No se venden ni se ceden a terceros. Los pagos en línea los procesa la pasarela de pagos, que recibe solo lo necesario para cobrar.

Sus derechos: conocer, actualizar y corregir sus datos, pedir prueba de esta autorización, saber cómo se han usado, revocar la autorización y pedir que se eliminen cuando no exista una obligación legal o contractual de conservarlos. Puede ejercerlos ante la biblioteca de su institución.

Al eliminar sus datos se borran su nombre y su correo; el registro de préstamos, multas y pagos se conserva sin identificarlo, porque sirve de soporte contable.

Al aceptar, autoriza a la institución a tratar sus datos para estos fines.');
