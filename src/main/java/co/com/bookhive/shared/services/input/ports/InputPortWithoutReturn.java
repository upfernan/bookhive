package co.com.bookhive.shared.services.input.ports;

public interface InputPortWithoutReturn<I> {

	void execute(I input);

}
