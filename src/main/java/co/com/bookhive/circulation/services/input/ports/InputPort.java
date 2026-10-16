package co.com.bookhive.circulation.services.input.ports;

public interface InputPort<T, R> {

	R execute(T data);

}
