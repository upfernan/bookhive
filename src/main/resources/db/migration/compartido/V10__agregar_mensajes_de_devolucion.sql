INSERT INTO mensaje (codigo, texto) VALUES
    ('loan.return.book-copy.required', 'Indique el ejemplar que se está devolviendo.'),
    ('loan.return.not-open', 'Ese ejemplar no tiene un préstamo abierto, así que no hay nada que devolver.'),
    ('loan.return.not-confirmed', 'No fue posible confirmar la devolución. Inténtelo de nuevo.'),
    ('email.fine-generated.subject', 'Se generó una multa por la devolución tardía de «{titulo}»'),
    ('email.fine-generated.body', E'Hola {nombre}:\n\nDevolviste el libro «{titulo}» después de la fecha límite, que era el {fecha}, y se generó una multa por los días de retraso. Puedes ver el valor y pagarla en BookHive.\n\nMientras la multa esté pendiente no podrás pedir nuevos préstamos.\n\nBookHive');
