package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDateUnit;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

/**
 * Reads a NetCDF file's header with netcdf-java, which handles NetCDF-3 and NetCDF-4 (HDF5) in pure
 * Java. Only the header is read, plus the first and last value of the time and spatial coordinates,
 * so the cost does not grow with the size of the data.
 *
 * <p>The file is opened from a {@link RandomAccessFile} on its path, never by location string, so
 * netcdf-java's handling of locations (URLs, NcML, decompressing a {@code .gz} to a cache directory)
 * never applies. Its signature is checked first, so only NetCDF and HDF5 files reach the parser.
 */
public final class NetcdfSummariser {

    static final int MAX_ATTRIBUTES = 40;
    static final int MAX_DIMENSIONS = 50;
    static final int MAX_VARIABLES = 200;
    static final int MAX_VALUE_CHARS = 500;

    // Each signature with the format name shown for it; netcdf-java calls NetCDF-4 plain HDF5.
    private static final byte[][] SIGNATURES = {
        {'C', 'D', 'F', 1},
        {'C', 'D', 'F', 2},
        {'C', 'D', 'F', 5},
        {(byte) 0x89, 'H', 'D', 'F', '\r', '\n', 0x1a, '\n'}
    };
    private static final String[] FORMATS = {
        "NetCDF-3 classic", "NetCDF-3 64-bit offset", "NetCDF-5 (CDF5)", "NetCDF-4 (HDF5)"
    };
    private static final List<String> AXIS_ORDER = List.of("latitude", "longitude", "x", "y");
    private static final Set<String> LATITUDE = Set.of("latitude", "grid_latitude");
    private static final Set<String> LONGITUDE = Set.of("longitude", "grid_longitude");
    private static final Set<String> LATITUDE_UNITS = Set.of("degrees_north", "degree_north", "degree_n", "degrees_n");
    private static final Set<String> LONGITUDE_UNITS = Set.of("degrees_east", "degree_east", "degree_e", "degrees_e");
    private static final int MAX_EXTENTS = 4;
    /**
     * The largest chunk a coordinate value is read from. Reading one value of a chunked (NetCDF-4)
     * variable decompresses its whole chunk, whose size the file sets.
     */
    static final long MAX_CHUNK_BYTES = 4L * 1024 * 1024;

    private NetcdfSummariser() {}

    /**
     * @param cancelled polled while the header is parsed; true stops the parse
     * @return the summary, or empty when the file is not NetCDF or netcdf-java cannot make sense of it
     * @throws IOException when the file cannot be opened or its signature read
     */
    public static Optional<NetcdfSummary> summarise(Path file, BooleanSupplier cancelled) throws IOException {
        String format = format(file);
        if (format == null) {
            return Optional.empty();
        }
        RandomAccessFile raf = new RandomAccessFile(file.toAbsolutePath().toString(), "r");
        NetcdfFile netcdf;
        try {
            netcdf = NetcdfFiles.open(raf, file.getFileName().toString(), cancel(cancelled), null);
        } catch (IOException | RuntimeException e) {
            // The signature was read, so this is the parser rejecting the content.
            raf.close();
            return Optional.empty();
        }
        if (netcdf == null) {
            raf.close();
            return Optional.empty();
        }
        try (netcdf) {
            // netcdf-java polls the cancel hook only in some of its loops, so check before reading data.
            if (cancelled.getAsBoolean()) {
                return Optional.empty();
            }
            return Optional.of(summary(netcdf, format));
        }
    }

    /** The format named by the file's signature, or null when it has none of the NetCDF ones. */
    private static String format(Path file) throws IOException {
        byte[] head;
        try (InputStream in = Files.newInputStream(file)) {
            head = in.readNBytes(8);
        }
        for (int i = 0; i < SIGNATURES.length; i++) {
            byte[] signature = SIGNATURES[i];
            if (head.length >= signature.length && Arrays.equals(head, 0, signature.length, signature, 0, signature.length)) {
                return FORMATS[i];
            }
        }
        return null;
    }

    private static NetcdfSummary summary(NetcdfFile netcdf, String format) {
        // Attributes named with a leading underscore are the library's own, such as _NCProperties.
        List<Attribute> globals = netcdf.getGlobalAttributes().stream()
            .filter(a -> !a.getShortName().startsWith("_"))
            .toList();
        List<NetcdfSummary.Attribute> attributes = globals.stream()
            .limit(MAX_ATTRIBUTES)
            .map(a -> new NetcdfSummary.Attribute(a.getShortName(), value(a)))
            .toList();

        List<NetcdfSummary.Dimension> dimensions = netcdf.getRootGroup().getDimensions().stream()
            .limit(MAX_DIMENSIONS)
            .map(d -> new NetcdfSummary.Dimension(d.getShortName(), d.getLength(), d.isUnlimited()))
            .toList();

        List<Variable> all = netcdf.getVariables();
        List<NetcdfSummary.Variable> variables = all.stream()
            .limit(MAX_VARIABLES)
            .map(v -> new NetcdfSummary.Variable(
                v.getFullName(),
                attribute(v, "long_name"),
                attribute(v, "standard_name"),
                attribute(v, "units"),
                v.getDimensionsString(),
                v.getDataType().toString(),
                v.isCoordinateVariable()))
            .toList();

        return new NetcdfSummary(
            format,
            attributes,
            globals.size() > MAX_ATTRIBUTES,
            dimensions,
            variables,
            all.size() > MAX_VARIABLES,
            time(all),
            extents(all));
    }

    private static NetcdfSummary.TimeCoverage time(List<Variable> variables) {
        for (Variable v : variables) {
            String units = attribute(v, "units");
            boolean time = "time".equalsIgnoreCase(attribute(v, "standard_name"))
                || "T".equalsIgnoreCase(attribute(v, "axis"))
                || (units != null && units.toLowerCase(Locale.ROOT).contains(" since "));
            if (!time || units == null || !oneDimensionalNumber(v)) {
                continue;
            }
            String calendar = attribute(v, "calendar");
            try {
                CalendarDateUnit unit = CalendarDateUnit.of(calendar, units);
                double[] span = firstAndLast(v);
                if (span == null) {
                    continue;
                }
                return new NetcdfSummary.TimeCoverage(
                    v.getFullName(),
                    unit.makeCalendarDate(span[0]).toString(),
                    unit.makeCalendarDate(span[1]).toString(),
                    calendar == null ? "standard" : calendar);
            } catch (IOException | RuntimeException e) {
                // Units or calendar netcdf-java cannot decode, or values it cannot read: no coverage.
            }
        }
        return null;
    }

    private static List<NetcdfSummary.Extent> extents(List<Variable> variables) {
        List<NetcdfSummary.Extent> extents = new ArrayList<>();
        for (Variable v : variables) {
            if (extents.size() == MAX_EXTENTS) {
                break;
            }
            String axis = spatialAxis(v);
            if (axis == null || !oneDimensionalNumber(v)) {
                continue;
            }
            try {
                double[] span = firstAndLast(v);
                if (span != null) {
                    extents.add(new NetcdfSummary.Extent(v.getFullName(), axis, attribute(v, "units"),
                        Math.min(span[0], span[1]), Math.max(span[0], span[1])));
                }
            } catch (IOException | RuntimeException e) {
                // A coordinate that cannot be read is left out.
            }
        }
        extents.sort(java.util.Comparator.comparingInt(e -> AXIS_ORDER.indexOf(e.axis())));
        return extents;
    }

    /** "latitude", "longitude", "x", "y", or null for a variable that is none of these. */
    private static String spatialAxis(Variable v) {
        String standardName = lower(attribute(v, "standard_name"));
        String units = lower(attribute(v, "units"));
        String axis = lower(attribute(v, "axis"));
        if (isIn(LATITUDE, standardName) || isIn(LATITUDE_UNITS, units)) {
            return "latitude";
        }
        if (isIn(LONGITUDE, standardName) || isIn(LONGITUDE_UNITS, units)) {
            return "longitude";
        }
        if ("projection_x_coordinate".equals(standardName) || (v.isCoordinateVariable() && "x".equals(axis))) {
            return "x";
        }
        if ("projection_y_coordinate".equals(standardName) || (v.isCoordinateVariable() && "y".equals(axis))) {
            return "y";
        }
        return null;
    }

    // Set.of(...).contains(null) throws, and most attributes are optional.
    private static boolean isIn(Set<String> values, String value) {
        return value != null && values.contains(value);
    }

    private static boolean oneDimensionalNumber(Variable v) {
        return v.getRank() == 1 && v.getDataType().isNumeric() && v.getSize() > 0;
    }

    /**
     * The first and last values, or null when either is not a finite number or reading them would
     * mean decompressing a chunk over {@link #MAX_CHUNK_BYTES}.
     */
    private static double[] firstAndLast(Variable v) throws IOException {
        if (chunkBytes(v) > MAX_CHUNK_BYTES) {
            return null;
        }
        int last = (int) v.getSize() - 1;
        try {
            double first = v.read(new int[] {0}, new int[] {1}).getDouble(0);
            double end = v.read(new int[] {last}, new int[] {1}).getDouble(0);
            return Double.isFinite(first) && Double.isFinite(end) ? new double[] {first, end} : null;
        } catch (InvalidRangeException e) {
            return null;
        }
    }

    /**
     * The bytes in one chunk of {@code v}, from the {@code _ChunkSizes} netcdf-java reports for a
     * chunked variable; 0 for contiguous storage (all of NetCDF-3), where a value is read alone.
     */
    private static long chunkBytes(Variable v) {
        Attribute sizes = v.findAttribute("_ChunkSizes");
        if (sizes == null) {
            return 0;
        }
        try {
            long bytes = Math.max(1, v.getElementSize());
            for (int i = 0; i < sizes.getLength(); i++) {
                Number size = sizes.getNumericValue(i);
                if (size == null || size.longValue() <= 0) {
                    return Long.MAX_VALUE;
                }
                bytes = Math.multiplyExact(bytes, size.longValue());
            }
            return bytes;
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static String attribute(Variable v, String name) {
        String value = v.findAttributeString(name, null);
        return value == null || value.isBlank() ? null : cut(value.strip());
    }

    private static String value(Attribute attribute) {
        if (attribute.isString()) {
            return cut(attribute.getStringValue() == null ? "" : attribute.getStringValue().strip());
        }
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < attribute.getLength() && values.length() <= MAX_VALUE_CHARS; i++) {
            if (i > 0) {
                values.append(", ");
            }
            values.append(attribute.getValue(i));
        }
        return cut(values.toString());
    }

    private static String cut(String value) {
        return value.length() <= MAX_VALUE_CHARS ? value : value.substring(0, MAX_VALUE_CHARS) + "…";
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static CancelTask cancel(BooleanSupplier cancelled) {
        return new CancelTask() {
            @Override
            public boolean isCancel() {
                return cancelled.getAsBoolean();
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public void setDone(boolean done) {}

            @Override
            public void setError(String message) {}

            @Override
            public void setProgress(String message, int progress) {}
        };
    }
}
