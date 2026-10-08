package co.com.bookhive.circulacion.servicios.input.ports;

public interface InputPort<T, R> {

	R ejecutar(T data);

}
