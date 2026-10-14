package co.com.bookhive.inicializador.migraciones;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.flywaydb.core.Flyway;

// Programa APARTE de la aplicación: aplica las migraciones y termina.
//
// No corre al arrancar el backend (spring.flyway.enabled=false) por dos razones:
// cada copia que levante Cloud Run intentaría migrar al tiempo, y hay que
// recorrer un esquema por institución, cosa que el arranque automático no sabe
// hacer. En el despliegue se ejecuta antes de publicar la versión nueva.
public final class MigracionesMain {

	private static final String ESQUEMA_COMPARTIDO = "compartido";
	private static final String RUTA_COMPARTIDO = "classpath:db/migration/compartido";
	private static final String RUTA_INSTITUCION = "classpath:db/migration/institucion";

	// Un nombre de esquema no puede ser cualquier cosa: entra en instrucciones
	// SQL, así que se exige el formato antes de usarlo.
	private static final Pattern NOMBRE_DE_ESQUEMA = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

	private MigracionesMain() {
		super();
	}

	public static void main(final String[] args) {

		final String url = variableObligatoria("DB_URL");
		final String usuario = variableObligatoria("DB_ADMIN_USERNAME");
		final String clave = variableObligatoria("DB_ADMIN_PASSWORD");

		try {
			migrarEsquemaCompartido(url, usuario, clave);

			final List<String> esquemas = leerEsquemasDeInstituciones(url, usuario, clave);
			System.out.println("Instituciones activas: " + esquemas.size());

			migrarInstituciones(url, usuario, clave, esquemas);

		} catch (final Exception excepcion) {
			System.err.println("Las migraciones fallaron: " + excepcion.getMessage());
			System.exit(1);
		}
	}

	private static void migrarEsquemaCompartido(
			final String url, final String usuario, final String clave) {

		Flyway.configure()
				.dataSource(url, usuario, clave)
				.schemas(ESQUEMA_COMPARTIDO)
				.locations(RUTA_COMPARTIDO)
				.createSchemas(true)
				.load()
				.migrate();

		System.out.println("Esquema compartido al día.");
	}

	private static List<String> leerEsquemasDeInstituciones(
			final String url, final String usuario, final String clave) throws Exception {

		final List<String> esquemas = new ArrayList<>();

		final String consulta =
				"SELECT esquema FROM " + ESQUEMA_COMPARTIDO + ".institucion"
				+ " WHERE estado = 'ACTIVA' ORDER BY esquema";

		try (Connection conexion = DriverManager.getConnection(url, usuario, clave);
			 PreparedStatement sentencia = conexion.prepareStatement(consulta);
			 ResultSet resultado = sentencia.executeQuery()) {

			while (resultado.next()) {
				esquemas.add(resultado.getString("esquema"));
			}
		}

		return esquemas;
	}

	private static void migrarInstituciones(
			final String url, final String usuario, final String clave,
			final List<String> esquemas) {

		final List<String> fallidas = new ArrayList<>();

		for (final String esquema : esquemas) {

			if (!NOMBRE_DE_ESQUEMA.matcher(esquema).matches()) {
				fallidas.add(esquema + " (nombre de esquema no válido)");
				continue;
			}

			try {
				Flyway.configure()
						.dataSource(url, usuario, clave)
						.schemas(esquema)
						.locations(RUTA_INSTITUCION)
						.createSchemas(true)
						.load()
						.migrate();

				System.out.println("  " + esquema + ": al día");

			} catch (final RuntimeException excepcion) {
				// Una institución que falla no detiene a las demás, pero el
				// programa termina en error: así el despliegue no continúa
				// creyendo que todas quedaron migradas (negativa 3 del ADR-015).
				fallidas.add(esquema + " (" + excepcion.getMessage() + ")");
				System.err.println("  " + esquema + ": FALLÓ");
			}
		}

		if (!fallidas.isEmpty()) {
			throw new IllegalStateException(
					"Quedaron instituciones sin migrar: " + String.join(", ", fallidas));
		}
	}

	private static String variableObligatoria(final String nombre) {
		final String valor = System.getenv(nombre);
		if (valor == null || valor.isBlank()) {
			System.err.println("Falta la variable de entorno " + nombre);
			System.exit(1);
		}
		return valor;
	}

}
