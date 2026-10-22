package co.com.bookhive.shared.services.usecase;

public interface UseCaseWithoutReturn<I> {

	void execute(I input);

}
