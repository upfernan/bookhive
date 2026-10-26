package co.com.bookhive.circulation.domain.location;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class LocationDomains {

	public static final int MAX_LEVELS = 6;
	public static final int MAX_LEVEL_NAME = 40;
	public static final int MAX_VALUE = 40;
	public static final int MAX_BRANCH_NAME = 100;

	private LocationDomains() {
		super();
	}

	public record Branch(UUID id, String name) {

		public Branch {
			id = UUIDHelper.getDefaultValue(id);
			name = TextHelper.getDefaultValue(name).trim();
		}

		public void validate() {
			if (TextHelper.isDefaultValue(name) || name.length() > MAX_BRANCH_NAME) {
				throw BookHiveException.create(MessageKey.BRANCH_NAME_INVALID, "Se pidió crear la sede \"" + name + "\".");
			}
		}

	}

	public record Level(int depth, String name) {
	}

	public record Spot(int depth, String level, String value) {
	}

	public record Levels(List<String> names) {

		public Levels {
			names = ObjectHelper.getDefaultValue(names, List.<String>of()).stream()
					.map(name -> TextHelper.getDefaultValue(name).trim())
					.toList();
		}

		public void validate() {
			final Set<String> seen = new HashSet<>();
			final boolean valid = !names.isEmpty() && names.size() <= MAX_LEVELS
					&& names.stream().allMatch(name -> !name.isEmpty() && name.length() <= MAX_LEVEL_NAME
							&& seen.add(name.toLowerCase(Locale.ROOT)));
			if (!valid) {
				throw BookHiveException.create(MessageKey.LOCATION_LEVELS_INVALID, "Se pidieron los niveles " + names + ".");
			}
		}

		public List<Level> levels() {
			return IntStream.range(0, names.size()).mapToObj(index -> new Level(index + 1, names.get(index))).toList();
		}

	}

	public record Relocation(String barcode, UUID branch, List<String> values) {

		public Relocation {
			barcode = TextHelper.getDefaultValue(barcode).trim();
			branch = UUIDHelper.getDefaultValue(branch);
			values = ObjectHelper.getDefaultValue(values, List.<String>of()).stream()
					.map(value -> TextHelper.getDefaultValue(value).trim())
					.toList();
		}

		public void validate(final int levelCount) {
			if (TextHelper.isDefaultValue(barcode)) {
				throw BookHiveException.create(MessageKey.BOOK_COPY_BARCODE_REQUIRED, "No llegó el código de barras.");
			}
			if (UUIDHelper.isDefaultValue(branch)) {
				throw BookHiveException.create(MessageKey.BRANCH_NOT_FOUND, "No llegó la sede del ejemplar " + barcode + ".");
			}
			final boolean valid = values.size() <= levelCount
					&& values.stream().allMatch(value -> !value.isEmpty() && value.length() <= MAX_VALUE);
			if (!valid) {
				throw BookHiveException.create(MessageKey.BOOK_COPY_LOCATION_INVALID,
						"La ubicación " + values + " no cabe en los " + levelCount + " niveles de la institución.");
			}
		}

	}

	public record BookCopyLocation(UUID bookCopy, String barcode, String status, Branch branch, List<Spot> spots) {

		public BookCopyLocation {
			spots = List.copyOf(ObjectHelper.getDefaultValue(spots, List.of()));
		}

		public String describe() {
			final StringBuilder text = new StringBuilder(branch.name());
			spots.forEach(spot -> text.append(" / ").append(spot.level()).append(' ').append(spot.value()));
			return text.toString();
		}

	}

}
