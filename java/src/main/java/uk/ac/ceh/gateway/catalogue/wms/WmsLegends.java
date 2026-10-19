package uk.ac.ceh.gateway.catalogue.wms;

import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The legends of a service's WMS layers, read from the map data definition that
 * templates/mapfile/_layers.map.ftl turns into MapServer layers: each attribute is a layer named
 * by the attribute, with a class per bucket then per value, and the features of a source are a
 * layer of one class. MapServer's GetStyles gives no rules for raster layers, so this is the
 * only source of the entries as data rather than as a legend image.
 */
final class WmsLegends {
    private WmsLegends() {
    }

    /**
     * @return entries by WMS layer name, in mapfile order; empty when there is no definition
     */
    static Map<String, List<LegendEntry>> of(MapDataDefinition definition) {
        if (definition == null || definition.getData() == null) {
            return Collections.emptyMap();
        }
        Map<String, List<LegendEntry>> legends = new LinkedHashMap<>();
        for (DataSource source : definition.getData()) {
            String shape = shape(source.getType());
            for (Attribute attribute : listOf(source.getAttributes())) {
                List<LegendEntry> entries = new ArrayList<>();
                listOf(attribute.getBuckets()).forEach(bucket -> add(entries, bucket.getLabel(), bucket.getStyle(), shape));
                listOf(attribute.getValues()).forEach(value -> add(entries, value.getLabel(), value.getStyle(), shape));
                put(legends, attribute.getName(), entries);
            }
            if (source.getFeatures() != null) {
                List<LegendEntry> entries = new ArrayList<>();
                add(entries, source.getFeatures().getLabel(), source.getFeatures().getStyle(), shape);
                put(legends, source.getFeatures().getName(), entries);
            }
        }
        return legends;
    }

    private static void add(List<LegendEntry> entries, String label, Style style, String shape) {
        if (label != null && !label.isBlank()) {
            entries.add(new LegendEntry(label.trim(), style == null ? null : style.getColour(), shape));
        }
    }

    private static void put(Map<String, List<LegendEntry>> legends, String layerName, List<LegendEntry> entries) {
        if (layerName != null && !layerName.isBlank() && !entries.isEmpty()) {
            legends.putIfAbsent(layerName.trim(), List.copyOf(entries));
        }
    }

    private static String shape(String type) {
        return switch (type == null ? "" : type.toUpperCase()) {
            case "LINE" -> "line";
            case "POINT" -> "point";
            default -> "area";
        };
    }

    private static <T> List<T> listOf(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
