package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.springframework.stereotype.Component;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Canonicalises a record's spatial reference system to an EPSG IRI.
 *
 * <p>Stored values arrive in many spellings (full OGC URIs, bare codes with a
 * URN code space, {@code EPSG:nnnn}, epsg.io links). Only a value that
 * reduces to an all-digit EPSG code, or to {@code CRS:84}, is mapped; anything
 * else (dataset URIs, {@code unknown}, blanks) yields {@code ""} so it is
 * omitted rather than coerced.
 */
@Component
public class CrsUri {

    private static final String EPSG_PREFIX = "http://www.opengis.net/def/crs/EPSG/0/";
    private static final String CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    private static final int EPSG_MIN_CODE = 1024;
    private static final int EPSG_MAX_CODE = 32767;
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final List<String> KNOWN_PREFIXES = List.of(
        EPSG_PREFIX,
        "https://www.opengis.net/def/crs/EPSG/0/",
        "urn:ogc:def:crs:EPSG::",
        "https://epsg.io/",
        "http://epsg.io/",
        "EPSG:"
    );

    public String canonical(SpatialReferenceSystem srs) {
        if (srs == null || srs.getCode() == null) {
            return "";
        }
        String code = srs.getCode().trim();
        if ("CRS:84".equalsIgnoreCase(code)) {
            return CRS84;
        }
        boolean prefixed = false;
        for (String prefix : KNOWN_PREFIXES) {
            if (code.regionMatches(true, 0, prefix, 0, prefix.length())) {
                code = code.substring(prefix.length()).trim();
                prefixed = true;
                break;
            }
        }
        if (!DIGITS.matcher(code).matches()) {
            return "";
        }
        if (!prefixed) {
            String codeSpace = srs.getCodeSpace();
            if (isBlank(codeSpace) ? !inEpsgRange(code) : !isEpsgCodeSpace(codeSpace)) {
                return "";
            }
        }
        return EPSG_PREFIX + code;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** A code space naming EPSG, such as {@code urn:ogc:def:crs:EPSG} or {@code EPSG}. */
    private static boolean isEpsgCodeSpace(String codeSpace) {
        return codeSpace.toUpperCase(Locale.ROOT).contains("EPSG");
    }

    /**
     * A bare code with no code space could be from any registry (ESRI's 102028, for
     * one), so it is read as EPSG only inside the range EPSG actually issues codes in.
     */
    private static boolean inEpsgRange(String digits) {
        if (digits.length() > 5 || digits.charAt(0) == '0') {
            return false;
        }
        int value = Integer.parseInt(digits);
        return value >= EPSG_MIN_CODE && value <= EPSG_MAX_CODE;
    }
}
