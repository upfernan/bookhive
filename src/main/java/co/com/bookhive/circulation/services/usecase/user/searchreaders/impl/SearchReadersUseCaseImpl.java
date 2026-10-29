package co.com.bookhive.circulation.services.usecase.user.searchreaders.impl;

import java.util.List;

import co.com.bookhive.circulation.domain.user.searchreaders.FoundReaderDomain;
import co.com.bookhive.circulation.domain.user.searchreaders.SearchReadersDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.usecase.user.searchreaders.SearchReadersUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class SearchReadersUseCaseImpl implements SearchReadersUseCase {

	private final UserRepository userRepository;
	private final SessionContextPort sessionContext;
	private final AuditPort audit;

	public SearchReadersUseCaseImpl(
			final UserRepository userRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit) {

		super();
		this.userRepository = requireDependency(userRepository, "el repositorio de usuarios");
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
	}

	@Override
	public List<FoundReaderDomain> execute(final SearchReadersDomain domain) {

		final SearchReadersDomain search = ObjectHelper.getDefaultValue(domain, new SearchReadersDomain(""))
				.withRepositories(userRepository);

		search.validate();

		final List<FoundReaderDomain> found = search.find();

		audit.register(
				AuditPort.READERS_SEARCHED,
				sessionContext.getInstitution(),
				sessionContext.getLibrarian(),
				found.size() + " resultado(s)");

		return found;
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso SearchReaders se creó sin " + name + ".");
		}
		return dependency;
	}

}
