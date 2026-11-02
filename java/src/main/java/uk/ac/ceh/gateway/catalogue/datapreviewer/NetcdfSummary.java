package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.List;

/**
 * What a NetCDF file's header says about its contents: enough to judge whether a gridded dataset
 * is usable without downloading it. No data values are included beyond the first and last of the
 * time and spatial coordinates.
 *
 * @param format              the file's format as netcdf-java names it, such as "NetCDF-4"
 * @param attributes          the global attributes, at most {@link NetcdfSummariser#MAX_ATTRIBUTES}
 * @param attributesTruncated true when the file has more global attributes than are listed
 * @param dimensions          the dimensions, at most {@link NetcdfSummariser#MAX_DIMENSIONS}
 * @param variables           the variables, at most {@link NetcdfSummariser#MAX_VARIABLES}
 * @param variablesTruncated  true when the file has more variables than are listed
 * @param time                the span of the time coordinate, or null when there is none or it
 *                            cannot be decoded
 * @param extents             the span of each spatial coordinate (latitude, longitude, x, y)
 */
public record NetcdfSummary(
    String format,
    List<Attribute> attributes,
    boolean attributesTruncated,
    List<Dimension> dimensions,
    List<Variable> variables,
    boolean variablesTruncated,
    TimeCoverage time,
    List<Extent> extents
) {

    public NetcdfSummary {
        attributes = List.copyOf(attributes);
        dimensions = List.copyOf(dimensions);
        variables = List.copyOf(variables);
        extents = List.copyOf(extents);
    }

    /** A global attribute, its value as text, cut to {@link NetcdfSummariser#MAX_VALUE_CHARS}. */
    public record Attribute(String name, String value) {}

    public record Dimension(String name, int length, boolean unlimited) {}

    /**
     * @param name         the full name, including any group path
     * @param longName     the {@code long_name} attribute, or null
     * @param standardName the CF {@code standard_name} attribute, or null
     * @param units        the {@code units} attribute, or null
     * @param dimensions   the dimension names, space-separated
     * @param type         the data type, such as "float"
     * @param coordinate   true for a coordinate variable, one named after its only dimension
     */
    public record Variable(
        String name,
        String longName,
        String standardName,
        String units,
        String dimensions,
        String type,
        boolean coordinate
    ) {}

    /**
     * @param variable the time coordinate's name
     * @param start    the first time, as an ISO 8601 date-time
     * @param end      the last time, as an ISO 8601 date-time
     * @param calendar the CF calendar, such as "standard" or "360_day"
     */
    public record TimeCoverage(String variable, String start, String end, String calendar) {}

    /**
     * @param variable the coordinate's name
     * @param axis     "latitude", "longitude", "x" or "y"
     * @param units    the coordinate's units, or null
     */
    public record Extent(String variable, String axis, String units, double min, double max) {}
}
