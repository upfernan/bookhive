package co.com.bookhive.circulation.services.usecase;

public interface UseCaseWithoutReturn<I> {

	void execute(I input);

}
