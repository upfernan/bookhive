package co.com.bookhive.circulation.services.usecase;

public interface UseCase<D, R> {

	R execute(D domain);

}
