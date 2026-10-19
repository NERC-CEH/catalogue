package uk.ac.ceh.gateway.catalogue.wms;

import lombok.Value;

/**
 * One class of a WMS layer's legend: what MapServer draws in that colour.
 */
@Value
public class LegendEntry {
    String label;
    /** As stored, e.g. #ff0000; the browser drops anything that is not a CSS colour. */
    String colour;
    /** How the swatch is drawn: "line", "point" or "area". */
    String shape;
}
