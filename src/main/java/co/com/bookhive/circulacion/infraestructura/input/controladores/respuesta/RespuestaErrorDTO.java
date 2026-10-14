package co.com.bookhive.circulacion.infraestructura.input.controladores.respuesta;

// Lo que ve el cliente cuando una solicitud no se puede atender:
// solo el mensaje para el usuario, nunca detalles internos.
public record RespuestaErrorDTO(String mensaje) {
}
