UPDATE mensaje
SET texto = 'No fue posible establecer quién está usando el sistema: la persona no está registrada o no está activa en BookHive.'
WHERE codigo = 'session.not-resolved';

INSERT INTO mensaje (codigo, texto) VALUES
    ('session.id-token.required', 'Debe enviar el pase que entregó el proveedor de identidad al iniciar sesión.'),
    ('session.not-opened', 'No fue posible iniciar la sesión. Vuelva a ingresar sus datos e inténtelo de nuevo.'),
    ('session.provider-unavailable', 'El servicio de inicio de sesión no está disponible en este momento. Inténtelo de nuevo en unos minutos.');
