package co.com.bookhive.catalog.infrastructure.output.repositories.semistructured.typesense;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import co.com.bookhive.catalog.services.output.ports.repositories.BookIndexRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookSearchPageEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.FacetCountEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class TypesenseBookIndexAdapter implements BookIndexRepository {

	static final String COLLECTION = "books";
	static final String SEARCH_KEY_LABEL = "bookhive-catalog-search";

	private static final String COLLECTIONS = "/collections";
	private static final String DOCUMENTS = COLLECTIONS + "/" + COLLECTION + "/documents";
	private static final String KEYS = "/keys";
	private static final String QUERY_BY = "title,authors,category,publisher";
	private static final String QUERY_BY_WEIGHTS = "4,2,1,1";
	private static final String SORT_BY = "_text_match:desc,title:asc";
	private static final String FACET_BY = "authors,category,publisher";
	private static final String MAX_FACET_VALUES = "20";
	private static final int BATCH_SIZE = 100;
	private static final int NOT_FOUND = 404;
	private static final int CONFLICT = 409;
	private static final int MAX_DETAIL_LENGTH = 200;
	private static final Pattern INSTITUTION = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");
	private static final MediaType JSON_LINES = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);
	private static final MediaType JSON_BODY = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
	private static final String FIELD_INSTITUTION = "institution";
	private static final String FIELD_BOOK = "book";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_AUTHORS = "authors";
	private static final String FIELD_CATEGORY = "category";
	private static final String FIELD_PUBLISHER = "publisher";
	private static final String FIELD_FORMAT = "format";
	private static final String FIELD_YEAR = "year";
	private static final String FIELD_ISBN = "isbn";
	private static final String TYPE_STRING = "string";
	private static final String FACET = "facet";

	private final TypesenseClient client;
	private final JsonMapper json = JsonMapper.shared();
	private final ReentrantLock readyLock = new ReentrantLock();
	private volatile boolean ready;

	public TypesenseBookIndexAdapter(final TypesenseClient client) {
		super();
		this.client = client;
	}

	@Override
	public BookSearchPageEntity search(final BookSearchCriteria criteria) {
		final BookSearchCriteria safeCriteria = ObjectHelper.getDefaultValue(criteria, BookSearchCriteria.countAll());
		final String institution = currentInstitution();
		requireReady();

		final Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("q", TextHelper.isDefaultValue(safeCriteria.text()) ? "*" : safeCriteria.text());
		parameters.put("query_by", QUERY_BY);
		parameters.put("query_by_weights", QUERY_BY_WEIGHTS);
		parameters.put("sort_by", SORT_BY);
		parameters.put("facet_by", FACET_BY);
		parameters.put("max_facet_values", MAX_FACET_VALUES);
		parameters.put("page", String.valueOf(safeCriteria.page()));
		parameters.put("per_page", String.valueOf(Math.max(safeCriteria.size(), 1)));
		final String filter = filterOf(safeCriteria);
		if (!filter.isEmpty()) {
			parameters.put("filter_by", filter);
		}

		final TypesenseResponse response = client.get(
				DOCUMENTS + "/search",
				parameters,
				ScopedSearchKey.forInstitution(searchKey(), institution));
		requireSuccess(response, "buscar en el catálogo de " + institution);

		return toPage(read(response.body()));
	}

	@Override
	public void save(final List<BookEntity> books) {
		final List<BookEntity> safeBooks = ObjectHelper.getDefaultValue(books, List.<BookEntity>of());
		if (safeBooks.isEmpty()) {
			return;
		}
		final String institution = currentInstitution();
		requireReady();

		for (int start = 0; start < safeBooks.size(); start += BATCH_SIZE) {
			importBooks(institution, safeBooks.subList(start, Math.min(start + BATCH_SIZE, safeBooks.size())));
		}
	}

	@Override
	public void delete(final List<UUID> books) {
		final List<UUID> safeBooks = ObjectHelper.getDefaultValue(books, List.<UUID>of());
		if (safeBooks.isEmpty()) {
			return;
		}
		final String institution = currentInstitution();
		requireReady();

		for (int start = 0; start < safeBooks.size(); start += BATCH_SIZE) {
			final String ids = safeBooks.subList(start, Math.min(start + BATCH_SIZE, safeBooks.size())).stream()
					.map(book -> "`" + book + "`")
					.collect(Collectors.joining(","));
			final TypesenseResponse response = client.delete(
					DOCUMENTS,
					Map.of("filter_by", "institution:=" + institution + " && book:=[" + ids + "]"),
					client.adminKey());
			requireSuccess(response, "retirar libros de " + institution);
		}
	}

	private void importBooks(final String institution, final List<BookEntity> books) {
		final String lines = books.stream()
				.map(book -> toDocument(institution, book))
				.collect(Collectors.joining("\n"));
		final TypesenseResponse response = client.post(
				DOCUMENTS + "/import",
				Map.of("action", "upsert"),
				client.adminKey(),
				lines,
				JSON_LINES);
		requireSuccess(response, "guardar libros de " + institution);

		final Optional<JsonNode> rejected = response.body().lines()
				.filter(line -> !line.isBlank())
				.map(this::read)
				.filter(result -> !result.path("success").asBoolean(false))
				.findFirst();
		if (rejected.isPresent()) {
			throw unavailable("Typesense rechazó un libro de " + institution + ": "
					+ abbreviate(rejected.get().path("error").asString("")));
		}
	}

	private String toDocument(final String institution, final BookEntity book) {
		final ObjectNode document = json.createObjectNode();
		document.put("id", institution + "_" + book.id());
		document.put(FIELD_INSTITUTION, institution);
		document.put(FIELD_BOOK, book.id().toString());
		document.put(FIELD_TITLE, book.title());
		final ArrayNode authors = document.putArray(FIELD_AUTHORS);
		book.authors().forEach(authors::add);
		document.put(FIELD_CATEGORY, book.category());
		document.put(FIELD_PUBLISHER, book.publisher());
		document.put(FIELD_FORMAT, book.format());
		if (book.year() > 0) {
			document.put(FIELD_YEAR, book.year());
		}
		if (!TextHelper.isDefaultValue(book.isbn())) {
			document.put(FIELD_ISBN, book.isbn());
		}
		return json.writeValueAsString(document);
	}

	private BookSearchPageEntity toPage(final JsonNode result) {
		final List<BookEntity> books = new ArrayList<>();
		result.path("hits").forEach(hit -> toBook(hit.path("document")).ifPresent(books::add));

		final Map<String, List<FacetCountEntity>> facets = new LinkedHashMap<>();
		result.path("facet_counts").forEach(facet -> facets.put(
				facet.path("field_name").asString(""),
				toFacetCounts(facet.path("counts"))));

		return new BookSearchPageEntity(
				result.path("found").asLong(0),
				books,
				facets.getOrDefault(FIELD_AUTHORS, List.of()),
				facets.getOrDefault(FIELD_CATEGORY, List.of()),
				facets.getOrDefault(FIELD_PUBLISHER, List.of()));
	}

	private static Optional<BookEntity> toBook(final JsonNode document) {
		final Optional<UUID> id = uuidOf(document.path(FIELD_BOOK).asString(""));
		if (id.isEmpty()) {
			return Optional.empty();
		}
		final List<String> authors = new ArrayList<>();
		document.path(FIELD_AUTHORS).forEach(author -> authors.add(author.asString("")));
		return Optional.of(new BookEntity(
				id.get(),
				document.path(FIELD_TITLE).asString(""),
				document.path(FIELD_ISBN).asString(""),
				document.path(FIELD_YEAR).asInt(0),
				document.path(FIELD_FORMAT).asString(""),
				BookEntity.STATUS_ACTIVE,
				document.path(FIELD_CATEGORY).asString(""),
				document.path(FIELD_PUBLISHER).asString(""),
				authors));
	}

	private static List<FacetCountEntity> toFacetCounts(final JsonNode counts) {
		final List<FacetCountEntity> values = new ArrayList<>();
		counts.forEach(count -> values.add(new FacetCountEntity(
				count.path("value").asString(""),
				count.path("count").asLong(0))));
		return values;
	}

	private static Optional<UUID> uuidOf(final String value) {
		try {
			return Optional.of(UUID.fromString(value));
		} catch (final IllegalArgumentException _) {
			return Optional.empty();
		}
	}

	private static String filterOf(final BookSearchCriteria criteria) {
		final List<String> filters = new ArrayList<>();
		if (!TextHelper.isDefaultValue(criteria.author())) {
			filters.add("authors:=`" + withoutBackticks(criteria.author()) + "`");
		}
		if (!TextHelper.isDefaultValue(criteria.category())) {
			filters.add("category:=`" + withoutBackticks(criteria.category()) + "`");
		}
		if (!TextHelper.isDefaultValue(criteria.publisher())) {
			filters.add("publisher:=`" + withoutBackticks(criteria.publisher()) + "`");
		}
		return String.join(" && ", filters);
	}

	private static String withoutBackticks(final String value) {
		return value.replace("`", "");
	}

	private void requireReady() {
		if (!client.isConfigured()) {
			throw unavailable("Falta bookhive.search.url o bookhive.search.api-key: no hay buscador configurado (ADR-021).");
		}
		if (ready) {
			return;
		}
		readyLock.lock();
		try {
			if (!ready) {
				ensureCollection();
				ensureSearchKey();
				ready = true;
			}
		} finally {
			readyLock.unlock();
		}
	}

	private void ensureCollection() {
		final TypesenseResponse existing = client.get(COLLECTIONS + "/" + COLLECTION, Map.of(), client.adminKey());
		if (existing.isSuccess()) {
			return;
		}
		if (existing.status() != NOT_FOUND) {
			requireSuccess(existing, "consultar la colección " + COLLECTION);
		}
		final TypesenseResponse created = client.post(
				COLLECTIONS, Map.of(), client.adminKey(), collectionSchema(), JSON_BODY);
		if (created.status() != CONFLICT) {
			requireSuccess(created, "crear la colección " + COLLECTION);
		}
	}

	private void ensureSearchKey() {
		final ObjectNode key = json.createObjectNode();
		key.put("description", "BookHive: búsqueda del catálogo, se restringe por institución");
		key.putArray("actions").add("documents:search");
		key.putArray("collections").add(COLLECTION);
		key.put("value", searchKey());
		final TypesenseResponse created = client.post(
				KEYS, Map.of(), client.adminKey(), json.writeValueAsString(key), JSON_BODY);
		if (created.status() != CONFLICT) {
			requireSuccess(created, "crear la clave de búsqueda");
		}
	}

	private String collectionSchema() {
		final ObjectNode schema = json.createObjectNode();
		schema.put("name", COLLECTION);
		final ArrayNode fields = schema.putArray("fields");
		fields.add(field(FIELD_INSTITUTION, TYPE_STRING));
		fields.add(field(FIELD_BOOK, TYPE_STRING));
		fields.add(field(FIELD_TITLE, TYPE_STRING).put("sort", true));
		fields.add(field(FIELD_AUTHORS, TYPE_STRING + "[]").put(FACET, true));
		fields.add(field(FIELD_CATEGORY, TYPE_STRING).put(FACET, true));
		fields.add(field(FIELD_PUBLISHER, TYPE_STRING).put(FACET, true));
		fields.add(field(FIELD_FORMAT, TYPE_STRING).put(FACET, true));
		fields.add(field(FIELD_YEAR, "int32").put("optional", true));
		fields.add(field(FIELD_ISBN, TYPE_STRING).put("optional", true));
		return json.writeValueAsString(schema);
	}

	private ObjectNode field(final String name, final String type) {
		return json.createObjectNode().put("name", name).put("type", type);
	}

	private String searchKey() {
		return ScopedSearchKey.derive(client.adminKey(), SEARCH_KEY_LABEL);
	}

	private JsonNode read(final String body) {
		try {
			return json.readTree(body);
		} catch (final JacksonException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.SEARCH_UNAVAILABLE,
					"Typesense devolvió una respuesta que no es JSON.");
		}
	}

	private static void requireSuccess(final TypesenseResponse response, final String action) {
		if (!response.isSuccess()) {
			throw unavailable("Typesense respondió " + response.status() + " al " + action + ": "
					+ abbreviate(response.body()));
		}
	}

	private static String currentInstitution() {
		final String institution = TenantContext.get();
		if (TenantContext.SCHEMA_WITHOUT_TENANT.equals(institution) || !INSTITUTION.matcher(institution).matches()) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se intentó usar el buscador sin una institución resuelta para la solicitud.");
		}
		return institution;
	}

	private static String abbreviate(final String text) {
		final String safeText = TextHelper.getDefaultValue(text);
		return safeText.length() <= MAX_DETAIL_LENGTH ? safeText : safeText.substring(0, MAX_DETAIL_LENGTH) + "...";
	}

	private static BookHiveException unavailable(final String technicalMessage) {
		return BookHiveException.createUnexpected(MessageKey.SEARCH_UNAVAILABLE, technicalMessage);
	}

}
