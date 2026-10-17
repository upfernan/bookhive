package co.com.bookhive.circulation.services.usecase;

public interface UseCaseWithReturn<I, O> {

	O execute(I input);

}
