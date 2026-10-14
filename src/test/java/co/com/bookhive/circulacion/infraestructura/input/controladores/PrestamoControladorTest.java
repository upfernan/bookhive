package co.com.bookhive.circulacion.infraestructura.input.controladores;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.memoria.BaseDatosMemoria;

// Prueba de punta a punta: arranca la aplicación completa y llama al endpoint
// POST /api/v1/prestamos como lo haría un cliente. Recorre controlador,
// adaptador de entrada, caso de uso y adaptadores de salida (en memoria).
@SpringBootTest
class PrestamoControladorTest {

	private static final String RUTA = "/api/v1/prestamos";

	@Autowired
	private WebApplicationContext contexto;

	private MockMvc mockMvc;

	@BeforeEach
	void preparar() {
		mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
	}

	@Test
	@DisplayName("presta un ejemplar disponible a un usuario al día y responde 201")
	void prestaUnEjemplarDisponible() throws Exception {
		mockMvc.perform(post(RUTA)
				.contentType(MediaType.APPLICATION_JSON)
				.content(peticion(BaseDatosMemoria.EJEMPLAR_DISPONIBLE_A, BaseDatosMemoria.USUARIO_AL_DIA)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.prestamo").exists())
				.andExpect(jsonPath("$.fechaVencimiento").exists());
	}

	@Test
	@DisplayName("rechaza el préstamo a un usuario con multas y responde 400 con el mensaje")
	void rechazaUsuarioConMultas() throws Exception {
		mockMvc.perform(post(RUTA)
				.contentType(MediaType.APPLICATION_JSON)
				.content(peticion(BaseDatosMemoria.EJEMPLAR_DISPONIBLE_B, BaseDatosMemoria.USUARIO_CON_MULTA)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje")
						.value("El usuario tiene multas pendientes y no puede llevar más ejemplares."));
	}

	@Test
	@DisplayName("rechaza el préstamo de un ejemplar que es solo de consulta en sala")
	void rechazaEjemplarSoloEnSala() throws Exception {
		mockMvc.perform(post(RUTA)
				.contentType(MediaType.APPLICATION_JSON)
				.content(peticion(BaseDatosMemoria.EJEMPLAR_SOLO_SALA, BaseDatosMemoria.USUARIO_AL_DIA)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje")
						.value("Este ejemplar solo puede consultarse en la sala; no se presta a domicilio."));
	}

	@Test
	@DisplayName("rechaza el préstamo de un ejemplar que no existe")
	void rechazaEjemplarInexistente() throws Exception {
		mockMvc.perform(post(RUTA)
				.contentType(MediaType.APPLICATION_JSON)
				.content(peticion(UUID.randomUUID(), BaseDatosMemoria.USUARIO_AL_DIA)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value("El ejemplar indicado no existe en el catálogo."));
	}

	private static String peticion(final UUID ejemplar, final UUID usuario) {
		return "{\"ejemplar\":\"" + ejemplar + "\",\"usuario\":\"" + usuario + "\"}";
	}

}
