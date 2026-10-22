package co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

public interface BookDetailsRow {

	UUID getId();

	String getTitle();

	String getIsbn();

	Integer getYear();

	String getFormat();

	String getStatus();

	String getCategory();

	String getPublisher();

	String getAuthors();

}
