package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialResolution;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns stored resolutions into valid typed RDF values: a spatial resolution as a
 * plain decimal number of metres ({@code xsd:decimal}) and a temporal resolution as
 * an {@code xsd:duration}.
 *
 * <p>An invalid typed literal freezes the whole graph, so anything that does not
 * map cleanly, including ambiguous or angular units, yields {@code ""}.
 */
@Component
@RequiredArgsConstructor
public class Resolutions {

    private static final String EPSG_PREFIX = "http://www.opengis.net/def/crs/EPSG/0/";
    private static final Pattern DECIMAL = Pattern.compile("\\d+(\\.\\d+)?|\\.\\d+");
    private static final Pattern DURATION = Pattern.compile("(?:(\\d+)\\s*([a-z]+))|(?:([a-z]+)\\s+([a-z]+))");
    private static final Pattern METRIC_EPSG = Pattern.compile("27700|2990[0-3]|3857|3035|326\\d\\d|327\\d\\d");

    private static final Map<String, BigDecimal> METRES_PER_UNIT = Map.ofEntries(
        Map.entry("urn:ogc:def:uom:epsg::9001", BigDecimal.ONE),
        Map.entry("m", BigDecimal.ONE),
        Map.entry("metre", BigDecimal.ONE),
        Map.entry("metres", BigDecimal.ONE),
        Map.entry("meter", BigDecimal.ONE),
        Map.entry("meters", BigDecimal.ONE),
        Map.entry("urn:ogc:def:uom:epsg::9036", BigDecimal.valueOf(1000)),
        Map.entry("km", BigDecimal.valueOf(1000)),
        Map.entry("kilometre", BigDecimal.valueOf(1000)),
        Map.entry("kilometres", BigDecimal.valueOf(1000)),
        Map.entry("kilometer", BigDecimal.valueOf(1000)),
        Map.entry("kilometers", BigDecimal.valueOf(1000))
    );

    private static final Map<String, String> UNIT_DESIGNATORS = Map.ofEntries(
        Map.entry("second", "TS"), Map.entry("seconds", "TS"), Map.entry("sec", "TS"), Map.entry("s", "TS"),
        Map.entry("minute", "TM"), Map.entry("minutes", "TM"), Map.entry("min", "TM"), Map.entry("mins", "TM"),
        Map.entry("hour", "TH"), Map.entry("hours", "TH"), Map.entry("hr", "TH"), Map.entry("h", "TH"),
        Map.entry("day", "D"), Map.entry("days", "D"), Map.entry("d", "D"),
        Map.entry("week", "7D"), Map.entry("weeks", "7D"),
        Map.entry("month", "M"), Map.entry("months", "M"),
        Map.entry("year", "Y"), Map.entry("years", "Y")
    );

    private static final Map<String, String> WORDS = Map.ofEntries(
        Map.entry("daily", "P1D"),
        Map.entry("hourly", "PT1H"),
        Map.entry("weekly", "P7D"),
        Map.entry("fortnightly", "P14D"),
        Map.entry("monthly", "P1M"),
        Map.entry("yearly", "P1Y"),
        Map.entry("annual", "P1Y"),
        Map.entry("annually", "P1Y"),
        Map.entry("decadal", "P10Y")
    );

    private static final Map<String, Integer> NUMBER_WORDS = Map.ofEntries(
        Map.entry("one", 1), Map.entry("two", 2), Map.entry("three", 3), Map.entry("four", 4),
        Map.entry("five", 5), Map.entry("six", 6), Map.entry("seven", 7), Map.entry("eight", 8),
        Map.entry("nine", 9), Map.entry("ten", 10)
    );

    private final CrsUri crsUri;

    /** The resolution's distance in metres as a plain decimal, or {@code ""}. */
    public String metres(SpatialResolution resolution, List<SpatialReferenceSystem> crs) {
        if (resolution == null || resolution.getDistance() == null) {
            return "";
        }
        String text = resolution.getDistance().trim();
        if (!DECIMAL.matcher(text).matches()) {
            return "";
        }
        BigDecimal distance = new BigDecimal(text);
        if (distance.signum() <= 0) {
            return "";
        }
        BigDecimal factor = factor(resolution.getUom(), crs);
        if (factor == null) {
            return "";
        }
        return distance.multiply(factor).stripTrailingZeros().toPlainString();
    }

    private BigDecimal factor(String uom, List<SpatialReferenceSystem> crs) {
        if (uom == null || uom.isBlank()) {
            return isMetric(crs) ? BigDecimal.ONE : null;
        }
        return METRES_PER_UNIT.get(uom.trim().toLowerCase(Locale.ROOT));
    }

    /** Metric only if some system maps to an EPSG code and every system that maps is metric. */
    private boolean isMetric(List<SpatialReferenceSystem> systems) {
        if (systems == null) {
            return false;
        }
        boolean anyMapped = false;
        for (SpatialReferenceSystem system : systems) {
            String canonical = crsUri.canonical(system);
            if (canonical.isEmpty()) {
                continue;
            }
            anyMapped = true;
            if (!canonical.startsWith(EPSG_PREFIX)
                || !METRIC_EPSG.matcher(canonical.substring(EPSG_PREFIX.length())).matches()) {
                return false;
            }
        }
        return anyMapped;
    }

    /** The text as an {@code xsd:duration}, or {@code ""} when it does not map cleanly. */
    private static final int MAX_QUANTITY = 1_000_000;

    public String duration(String text) {
        if (text == null) {
            return "";
        }
        String value = text.trim().toLowerCase(Locale.ROOT);
        String word = WORDS.get(value);
        if (word != null) {
            return word;
        }
        Matcher m = DURATION.matcher(value);
        if (!m.matches()) {
            return "";
        }
        String quantity = m.group(1) != null ? m.group(1) : m.group(3);
        String unit = m.group(1) != null ? m.group(2) : m.group(4);
        String designator = UNIT_DESIGNATORS.get(unit);
        Integer n = quantity(quantity);
        // The cap keeps n * 7 inside int and every value within what Jena's
        // xsd:duration validator accepts (it rejects P2800000000D): an ill-typed
        // literal would freeze the whole export, and no real resolution is this large.
        if (designator == null || n == null || n <= 0 || n > MAX_QUANTITY) {
            return "";
        }
        if (designator.equals("7D")) {
            return "P" + (n * 7) + "D";
        }
        return designator.startsWith("T") ? "PT" + n + designator.substring(1) : "P" + n + designator;
    }

    private static Integer quantity(String text) {
        if (Character.isDigit(text.charAt(0))) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return NUMBER_WORDS.get(text);
    }
}
