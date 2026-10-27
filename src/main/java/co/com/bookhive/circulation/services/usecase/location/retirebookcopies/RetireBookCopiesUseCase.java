package co.com.bookhive.circulation.services.usecase.location.retirebookcopies;

import java.util.UUID;

public interface RetireBookCopiesUseCase {

	int execute(UUID book);

}
