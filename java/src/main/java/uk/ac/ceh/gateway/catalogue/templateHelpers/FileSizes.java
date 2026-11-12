package uk.ac.ceh.gateway.catalogue.templateHelpers;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A file size for people to read, for templates: <code>${fileSizes.format(file.bytes)}</code>.
 * Gives exactly what web/src/file-size/formatBytes.js gives: binary multiples (a kilobyte is 1024
 * bytes) named B, KB, MB, GB and TB, to at most two decimal places.
 */
public class FileSizes {
    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB"};

    public String format(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        int unit = Math.min((63 - Long.numberOfLeadingZeros(bytes)) / 10, UNITS.length - 1);
        BigDecimal value = scaled(bytes, unit);
        // 1023.999 KB rounds to 1024 KB: that is 1 MB
        if (value.compareTo(BigDecimal.valueOf(1024)) >= 0 && unit < UNITS.length - 1) {
            unit += 1;
            value = scaled(bytes, unit);
        }
        return value.stripTrailingZeros().toPlainString() + " " + UNITS[unit];
    }

    private static BigDecimal scaled(long bytes, int unit) {
        return BigDecimal.valueOf(bytes)
            .divide(BigDecimal.valueOf(1L << (10 * unit)), 2, RoundingMode.HALF_UP);
    }
}
