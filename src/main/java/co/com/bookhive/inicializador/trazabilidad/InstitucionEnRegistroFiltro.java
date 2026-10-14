package co.com.bookhive.inicializador.trazabilidad;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Pone la institución de la petición en el MDC, para que cada línea del
// registro diga de qué universidad era. Sin esto, los registros de todas las
// instituciones quedan revueltos y no se puede investigar nada.
// El identificador de traza y el de tramo los pone micrometer solo; esta es
// la parte propia de BookHive.
@Component
public class InstitucionEnRegistroFiltro extends OncePerRequestFilter {

	static final String CLAVE_MDC = "institucion";
	static final String SIN_RESOLVER = "sin-institucion";

	@Override
	protected void doFilterInternal(
			final HttpServletRequest peticion,
			final HttpServletResponse respuesta,
			final FilterChain cadena) throws ServletException, IOException {

		try {
			MDC.put(CLAVE_MDC, resolverInstitucion());
			cadena.doFilter(peticion, respuesta);
		} finally {
			// EL finally NO ES OPCIONAL. El MDC vive en el hilo, y los hilos se
			// reutilizan entre peticiones: si no se limpia, la siguiente petición
			// hereda la institución de la anterior y los registros mienten.
			// Es el mismo problema de la conexión que no restaura su rol (ADR-011).
			MDC.remove(CLAVE_MDC);
		}
	}

	private String resolverInstitucion() {
		// Cuando entre el proveedor de identidad, la institución sale del token
		// (ADR-028) y nunca de una cabecera, que el cliente podría falsear.
		return SIN_RESOLVER;
	}

}
