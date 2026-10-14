package co.com.bookhive.circulacion.infraestructura.output.repositorios.memoria;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.EjemplarEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.MultaEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.PrestamoEntidad;
import co.com.bookhive.transversal.helper.ObjectHelper;

// TEMPORAL: hace de base de datos mientras no existe PostgreSQL.
// Guarda las mismas entidades que irán a las tablas, pero en memoria:
// todo se borra al apagar la aplicación.
//
// Trae datos de prueba con identificadores fijos para poder probar el
// endpoint POST /api/v1/prestamos desde Postman o desde las pruebas.
@Component
public class BaseDatosMemoria {

	public static final UUID EJEMPLAR_DISPONIBLE_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID EJEMPLAR_DISPONIBLE_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final UUID EJEMPLAR_SOLO_SALA = UUID.fromString("33333333-3333-3333-3333-333333333333");
	public static final UUID USUARIO_AL_DIA = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	public static final UUID USUARIO_CON_MULTA = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	private final Map<UUID, EjemplarEntidad> ejemplares = new ConcurrentHashMap<>();
	private final Map<UUID, PrestamoEntidad> prestamos = new ConcurrentHashMap<>();
	private final Map<UUID, MultaEntidad> multas = new ConcurrentHashMap<>();

	public BaseDatosMemoria() {
		super();
		agregarEjemplar(EJEMPLAR_DISPONIBLE_A, true);
		agregarEjemplar(EJEMPLAR_DISPONIBLE_B, true);
		agregarEjemplar(EJEMPLAR_SOLO_SALA, false);
		agregarMultaPendiente(USUARIO_CON_MULTA);
	}

	public EjemplarEntidad buscarEjemplar(final UUID identificador) {
		return ObjectHelper.obtenerValorDefecto(ejemplares.get(identificador), new EjemplarEntidad());
	}

	public boolean existeEjemplar(final UUID identificador) {
		return ejemplares.containsKey(identificador);
	}

	public Collection<PrestamoEntidad> listarPrestamos() {
		return prestamos.values();
	}

	public void guardarPrestamo(final PrestamoEntidad prestamo) {
		prestamos.put(prestamo.getIdentificador(), prestamo);
	}

	public Collection<MultaEntidad> listarMultas() {
		return multas.values();
	}

	private void agregarEjemplar(final UUID identificador, final boolean prestable) {
		final EjemplarEntidad ejemplar = new EjemplarEntidad();
		ejemplar.setIdentificador(identificador);
		ejemplar.setLibro(UUID.randomUUID());
		ejemplar.setSede(UUID.randomUUID());
		ejemplar.setCodigoBarras("EJ-" + identificador.toString().substring(0, 4));
		ejemplar.setEstado(EjemplarEntidad.ESTADO_DISPONIBLE);
		ejemplar.setPrestable(prestable);
		ejemplares.put(identificador, ejemplar);
	}

	private void agregarMultaPendiente(final UUID usuario) {
		final MultaEntidad multa = new MultaEntidad();
		multa.setIdentificador(UUID.randomUUID());
		multa.setPrestamo(UUID.randomUUID());
		multa.setUsuario(usuario);
		multa.setFechaGeneracion(LocalDate.of(2026, 9, 1));
		multa.setEstado(MultaEntidad.ESTADO_PENDIENTE);
		multas.put(multa.getIdentificador(), multa);
	}

}
