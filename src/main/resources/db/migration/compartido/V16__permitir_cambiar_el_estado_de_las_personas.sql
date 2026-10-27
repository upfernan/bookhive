GRANT UPDATE (estado, identidad) ON persona TO ${appUser};

INSERT INTO mensaje (codigo, texto) VALUES
    ('user.purged', 'Los datos personales de esa cuenta ya fueron depurados; la cuenta no se puede reactivar.'),
    ('user.has-active-loans', 'No se pueden depurar los datos porque la persona tiene préstamos sin devolver.'),
    ('user.has-pending-fines', 'No se pueden depurar los datos porque la persona tiene multas sin pagar.'),
    ('user.retention.invalid', 'El plazo de conservación debe estar entre 1 y 120 meses.');
