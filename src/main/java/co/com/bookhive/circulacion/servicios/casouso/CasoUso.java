package co.com.bookhive.circulacion.servicios.casouso;

// Contrato genérico de todos los casos de uso: reciben dominio y devuelven dominio.
// Es el par de InputPort, pero del lado del caso de uso: aquí nunca entra un DTO.
public interface CasoUso<D, R> {

	R ejecutar(D dominio);

}
