package co.com.bookhive.shared.services.input.ports;

public interface InputPortWithReturn<I, O> {

	O execute(I input);

}
