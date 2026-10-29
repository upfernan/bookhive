package co.com.bookhive.access.domain.institution.listactiveschemas;

import java.util.List;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record ListActiveSchemasDomain(List<String> schemas) {

	public ListActiveSchemasDomain {
		schemas = List.copyOf(ObjectHelper.getDefaultValue(schemas, List.of()));
	}

}
