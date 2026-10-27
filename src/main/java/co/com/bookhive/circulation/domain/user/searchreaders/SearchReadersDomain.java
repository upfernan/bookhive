package co.com.bookhive.circulation.domain.user.searchreaders;

import java.util.List;
import java.util.Optional;

import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class SearchReadersDomain {

	public static final int MIN_LENGTH = 3;
	public static final int MAX_LENGTH = 100;
	public static final int LIMIT = 10;

	private String text;

	private UserRepository userRepository;

	public SearchReadersDomain(final String text) {
		super();
		setText(text);
	}

	public SearchReadersDomain withRepositories(final UserRepository users) {
		final SearchReadersDomain withPorts = new SearchReadersDomain(text);
		withPorts.userRepository = users;
		return withPorts;
	}

	public void validate() {
		requireRepositories();

		if (text.length() < MIN_LENGTH || text.length() > MAX_LENGTH) {
			throw BookHiveException.create(
					MessageKey.USER_SEARCH_TEXT_INVALID,
					"La búsqueda de lectores llegó con " + text.length() + " caracteres; se aceptan entre "
							+ MIN_LENGTH + " y " + MAX_LENGTH + ".");
		}
	}

	public List<FoundReaderDomain> find() {
		requireRepositories();

		return userRepository.findByCriteria(UserCriteria.matching(text, LIMIT)).stream()
				.limit(LIMIT)
				.map(user -> new FoundReaderDomain(user.getId(), user.getName(), user.getEmail()))
				.toList();
	}

	public String getText() {
		return text;
	}

	private void requireRepositories() {
		if (ObjectHelper.isNull(userRepository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se usó SearchReadersDomain sin repositorio; falta llamar withRepositories.");
		}
	}

	private void setText(final String text) {
		this.text = Optional.ofNullable(text).map(String::trim).orElse(TextHelper.getDefaultValue());
	}

}
