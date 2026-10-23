INSERT INTO mensaje (codigo, texto) VALUES
    ('messaging.unavailable', 'No fue posible dejar el aviso en la cola en este momento. Se volverá a intentar automáticamente.'),
    ('email.unavailable', 'El servicio de correo no está disponible en este momento. El aviso se volverá a intentar automáticamente.'),
    ('notification.data.invalid', 'El aviso recibido no trae los datos necesarios para enviarse.'),
    ('email.loan-registered.subject', 'Quedó registrado tu préstamo de «{titulo}»'),
    ('email.loan-registered.body', E'Hola {nombre}:\n\nQuedó registrado el préstamo del libro «{titulo}». Debes devolverlo a más tardar el {fecha}.\n\nBookHive'),
    ('email.loan-due-soon.subject', 'Tu préstamo vence el {fecha}'),
    ('email.loan-due-soon.body', E'Hola {nombre}:\n\nEl libro «{titulo}» que tienes prestado en la biblioteca debe devolverse a más tardar el {fecha}. Si ya lo devolviste, puedes ignorar este mensaje.\n\nBookHive');
