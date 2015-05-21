package uk.ac.ceh.gateway.catalogue.services;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * The following service allows us to process well known text geometries into 
 * either bounding boxes or points. These are presented in the native spatial 
 * solr format.
 * @author cjohn
 */
public class SolrGeometryService {
    private final WKTReader reader;
    
    public SolrGeometryService(WKTReader reader) {
        this.reader = reader;
    }
    
    public String toSolrGeometry(String wkt) {
        try {
            Geometry geom = reader.read(wkt).getEnvelope();
            if(geom.isEmpty()) {
                return null;
            }
            else if (geom instanceof Point) {
                Coordinate coordinate = geom.getCoordinate();
                return new StringBuilder()
                        .append(coordinate.x).append(" ")
                        .append(coordinate.y).toString();
            }
            else {
                Coordinate[] coordinates = geom.getCoordinates();
                return new StringBuilder()
                        .append(coordinates[0].x).append(" ")
                        .append(coordinates[0].y).append(" ")
                        .append(coordinates[2].x).append(" ")
                        .append(coordinates[2].y).toString();
            }
        }
        catch(ParseException pe) {
            return null;
        }
    } 
}
