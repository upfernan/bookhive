package co.com.bookhive.shared.services.usecase;

public interface UseCaseWithReturn<I, O> {

	O execute(I input);

}
