package uk.ac.ceh.gateway.catalogue.people;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An ORCID iD, as an editor may type it: bare ({@code 0000-0002-1825-0097}), with or without a scheme
 * or {@code www.}, and with a lower-case {@code x}. Only an iD whose check character is right is one:
 * the last character is an ISO 7064 MOD 11-2 checksum of the other fifteen digits, so a mistyped or
 * transposed digit is caught here rather than sent to ORCID's registry (dri-one #534).
 */
public final class Orcid {

    public static final String PREFIX = "https://orcid.org/";

    private static final Pattern FORM = Pattern.compile(
        "^(?:(?:https?://)?(?:www\\.)?orcid\\.org/)?(\\d{4}-\\d{4}-\\d{4}-\\d{3}[\\dXx])/?$"
    );

    private Orcid() {}

    /** The bare iD, upper-cased, of a value that is a well-formed ORCID with a valid check character */
    public static Optional<String> id(String value) {
        if (value == null) return Optional.empty();
        var matcher = FORM.matcher(value.strip());
        if (!matcher.matches()) return Optional.empty();
        var id = matcher.group(1).toUpperCase(Locale.ROOT);
        return checksumValid(id) ? Optional.of(id) : Optional.empty();
    }

    /** {@code https://orcid.org/<id>}, the form records store and ORCID asks an iD to be shown in */
    public static String uri(String id) {
        return PREFIX + id;
    }

    static boolean checksumValid(String id) {
        var digits = id.replace("-", "");
        var total = 0;
        for (var i = 0; i < digits.length() - 1; i++) {
            total = (total + Character.digit(digits.charAt(i), 10)) * 2;
        }
        var result = (12 - total % 11) % 11;
        var expected = result == 10 ? 'X' : Character.forDigit(result, 10);
        return digits.charAt(digits.length() - 1) == expected;
    }
}
