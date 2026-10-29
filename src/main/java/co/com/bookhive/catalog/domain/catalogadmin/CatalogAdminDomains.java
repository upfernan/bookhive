package co.com.bookhive.catalog.domain.catalogadmin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class CatalogAdminDomains {

	public static final String CATEGORY_ACTIVE = "ACTIVA";
	public static final String CATEGORY_INACTIVE = "INACTIVA";
	public static final String TITLE_ACTIVE = "ACTIVO";
	public static final String TITLE_WITHDRAWN = "BAJA";
	public static final String PHYSICAL = "FISICO";
	public static final String DIGITAL = "DIGITAL";

	private static final Pattern ISBN = Pattern.compile("\\d{9}[\\dX]|\\d{13}");
	private static final int MAX_CATEGORY = 100;
	private static final int MAX_TITLE = 300;
	private static final int MAX_NAME = 150;
	private static final int MAX_ISBN = 17;
	private static final int FIRST_YEAR = 1000;
	private static final int LAST_YEAR = 2100;
	private static final int MAX_BARCODE = 50;

	private CatalogAdminDomains() {
		super();
	}

	public record Category(UUID id, String name, String status, long activeTitles) {

		public Category {
			id = UUIDHelper.getDefaultValue(id);
			name = TextHelper.getDefaultValue(name).trim();
			status = TextHelper.isDefaultValue(status) ? CATEGORY_ACTIVE : TextHelper.getDefaultValue(status);
			activeTitles = Math.max(activeTitles, 0);
		}

		public void validate() {
			if (TextHelper.isDefaultValue(name) || name.length() > MAX_CATEGORY) {
				throw BookHiveException.create(MessageKey.CATEGORY_NAME_INVALID, "Se pidió la categoría \"" + name + "\".");
			}
		}

		public boolean isActive() {
			return CATEGORY_ACTIVE.equals(status);
		}

		public boolean sameName(final String other) {
			return name.toLowerCase(Locale.ROOT).equals(TextHelper.getDefaultValue(other).trim().toLowerCase(Locale.ROOT));
		}

	}

	public record CategoryStatusChange(UUID id, boolean active) {

		public CategoryStatusChange {
			id = UUIDHelper.getDefaultValue(id);
		}

	}

	public record Title(
			UUID id,
			String title,
			String isbn,
			int year,
			String format,
			String status,
			UUID category,
			String publisher,
			List<String> authors,
			UUID branch,
			List<String> barcodes) {

		public Title {
			id = UUIDHelper.getDefaultValue(id);
			title = TextHelper.getDefaultValue(title).trim();
			isbn = TextHelper.getDefaultValue(isbn).trim().replace("-", "").toUpperCase(Locale.ROOT);
			year = Math.max(year, 0);
			format = TextHelper.getDefaultValue(format).trim().toUpperCase(Locale.ROOT);
			status = TextHelper.getDefaultValue(status).trim();
			category = UUIDHelper.getDefaultValue(category);
			publisher = TextHelper.getDefaultValue(publisher).trim();
			authors = clean(authors);
			branch = UUIDHelper.getDefaultValue(branch);
			barcodes = clean(barcodes);
		}

		public void validate() {
			final Set<String> seen = new HashSet<>();
			final boolean valid = !title.isEmpty() && title.length() <= MAX_TITLE
					&& (PHYSICAL.equals(format) || DIGITAL.equals(format))
					&& !publisher.isEmpty() && publisher.length() <= MAX_NAME
					&& !authors.isEmpty()
					&& authors.stream().allMatch(author -> author.length() <= MAX_NAME && seen.add(author.toLowerCase(Locale.ROOT)))
					&& (isbn.isEmpty() || isbn.length() <= MAX_ISBN && ISBN.matcher(isbn).matches())
					&& (year == 0 || year >= FIRST_YEAR && year <= LAST_YEAR);
			if (!valid) {
				throw BookHiveException.create(MessageKey.BOOK_TITLE_DATA_INVALID, "Se pidió el título \"" + title + "\".");
			}
			if (UUIDHelper.isDefaultValue(category)) {
				throw BookHiveException.create(MessageKey.CATEGORY_NOT_FOUND, "No llegó la categoría del título.");
			}
			final boolean copiesValid = barcodes.isEmpty()
					|| PHYSICAL.equals(format) && !UUIDHelper.isDefaultValue(branch)
							&& barcodes.stream().distinct().count() == barcodes.size()
							&& barcodes.stream().allMatch(barcode -> barcode.length() <= MAX_BARCODE);
			if (!copiesValid) {
				throw BookHiveException.create(MessageKey.BOOK_TITLE_COPIES_INVALID,
						"Ejemplares inválidos para \"" + title + "\": " + barcodes + ".");
			}
		}

		public Title withId(final UUID newId, final String newStatus) {
			return new Title(newId, title, isbn, year, format, newStatus, category, publisher, authors, branch, barcodes);
		}

		public boolean isWithdrawn() {
			return TITLE_WITHDRAWN.equals(status);
		}

		private static List<String> clean(final List<String> values) {
			return ObjectHelper.getDefaultValue(values, List.<String>of()).stream()
					.map(value -> TextHelper.getDefaultValue(value).trim())
					.filter(value -> !value.isEmpty())
					.toList();
		}

	}

}
