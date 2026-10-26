INSERT INTO mensaje (codigo, texto) VALUES
    ('book-copy.location.invalid', 'La ubicación no es válida: llene los niveles en orden, sin dejar uno vacío, con máximo 40 caracteres cada uno y sin pasar los niveles que tiene la institución.'),
    ('book-copy.withdrawn', 'Ese ejemplar ya fue dado de baja de la biblioteca.'),
    ('book-copy.not-withdrawable', 'Solo se puede dar de baja un ejemplar que esté disponible: no puede estar prestado ni apartado para una reserva.'),
    ('branch.name.invalid', 'El nombre de la sede es obligatorio y puede tener máximo 100 caracteres.'),
    ('branch.name.taken', 'Ya existe una sede con ese nombre.'),
    ('branch.not-found', 'La sede indicada no existe en la institución.'),
    ('location.levels.invalid', 'Defina entre 1 y 6 niveles de ubicación, cada uno con un nombre distinto de máximo 40 caracteres.'),
    ('location.levels.in-use', 'No se puede quitar ese nivel porque hay ejemplares ubicados en él. Primero cambie su ubicación.');
