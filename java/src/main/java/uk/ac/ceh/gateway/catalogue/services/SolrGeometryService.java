package uk.ac.ceh.gateway.catalogue.services;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The following service allows us to process well known text geometries into 
 * either bounding boxes or points. These are presented in the native spatial 
 * solr format.
 */
public class SolrGeometryService {
    private final WKTReader reader = new WKTReader();
    
    public String toSolrGeometry(String wkt) {
        try {
            Geometry geom = reader.read(wkt);
            if(geom.isEmpty()) {
                return null;
            }

            Geometry envelope = geom.getEnvelope();
            if (envelope instanceof Point) {
                Coordinate coordinate = envelope.getCoordinate();
                return new StringBuilder()
                        .append(coordinate.x).append(" ")
                        .append(coordinate.y).toString();
            }
            else if (envelope instanceof LineString) {
                Coordinate[] coordinates = envelope.getCoordinates();
                return new StringBuilder("ENVELOPE(")
                        .append(coordinates[0].x).append(",")
                        .append(coordinates[1].x).append(",")
                        .append(coordinates[1].y).append(",")
                        .append(coordinates[0].y).append(")")
                        .toString();
            }
            else {
                Coordinate[] coordinates = envelope.getCoordinates();
                return new StringBuilder("ENVELOPE(")
                        .append(coordinates[0].x).append(",")
                        .append(coordinates[2].x).append(",")
                        .append(coordinates[2].y).append(",")
                        .append(coordinates[0].y).append(")")
                        .toString();
            }
        }
        catch(NullPointerException | ParseException e) {
            return null;
        }
    }
    
    // Takes a list of wkt 
    public List<String> toSolrGeometry(List<String> wktList) {
        return wktList
                .stream()
                .filter(Objects::nonNull)
                .map( w -> toSolrGeometry(w))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
