package uk.ac.ceh.gateway.catalogue.services;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

/**
 * The following service will take a wms url, layer and tms coordiantes then
 * return a WMS GetMap Request url for the desired tile.
 * @author cjohn
 */
@AllArgsConstructor
public class TMSToWMSGetMapService {
    private final DecimalFormat bboxPartFormat;
    private final int height, width;
    
    public TMSToWMSGetMapService() {
        this(new DecimalFormat("#.##"), 256, 256);
    }
    
    public String getWMSMapRequest(String url, String layer, int z, int x, int y) {
        return new StringBuilder(convertToValidWMSEndpoint(url))
                .append("LAYERS=").append(layer)
                .append("&BBOX=").append(getBBoxParam(z,x,y))
                .append("&WIDTH=").append(Integer.toString(width))
                .append("&HEIGHT=").append(Integer.toString(height))
                .append("&STYLES=")
                .append("&TRANSPARENT=TRUE")
                .append("&FORMAT=image%2Fpng")
                .append("&SRS=EPSG%3A3857")
                .append("&VERSION=1.1.1")
                .append("&SERVICE=WMS")
                .append("&REQUEST=GetMap&")
                .toString();
    }
    
    /**
     * Get the bbox component in a wms friendly string
     * @see #getBBox(int, int, int) 
     * @param z zoom level
     * @param x coordinate 
     * @param y coordinate
     * @return a string bbox
     */
    public String getBBoxParam(int z, int x, int y) {
        return Arrays.stream(getBBox(z,x,y))
                     .mapToObj((p) -> bboxPartFormat.format(p))
                     .collect(Collectors.joining(","));
    }
    
    /**
     * In EPSG:3857 the world is represented as a square with extent 
     * -20037508.34,-20037508.34,20037508.34,20037508.34. The tiled map service
     * scheme splits quadtrees this square into tiles which can be addressed by
     * 3 coordinates z, x, and y. So for example at zoom level 1 the world is
     * split into four squares:
     *                             ___________
     *                            |     |     |
     *                            | 0,1 | 1,1 |
     *                            |_____|_____|
     *                            |     |     |
     *                            | 0,0 | 1,0 |
     *                            |_____|_____|
     * 
     * And at zoom level 2 the world is split into sixteen squares:
     *                      _________________________
     *                      |     |     |     |     |
     *                      | 0,3 | 1,3 | 2,3 | 3,3 |
     *                      |_____|_____|_____|_____|
     *                      |     |     |     |     |
     *                      | 0,2 | 1,2 | 2,2 | 3,2 |
     *                      |_____|_____|_____|_____|
     *                      |     |     |     |     |
     *                      | 0,1 | 1,1 | 2,1 | 3,1 |
     *                      |_____|_____|_____|_____|
     *                      |     |     |     |     |
     *                      | 0,0 | 1,0 | 2,0 | 3,0 |
     *                      |_____|_____|_____|_____|
     * 
     * This method will generate the EPSG:3857 bounding box for the given zoom
     * level and the x, y coordinates.
     * @param z zoom level
     * @param x coordinate. The above diagrams are in the form x,y
     * @param y coordinate. The above diagrams are in the form x,y
     * @return a bbox array in the form [minx,miny,maxx,maxxy]
     */
    public double[] getBBox(int z, int x, int y) {
        double min = -20037508.34, max = 20037508.34;
        double extent = max - min;
        
        int squares = (int)Math.pow(2, z);
        
        double tileExtent = extent / squares;
        
        double minX = (tileExtent * x) + min;
        double minY = (tileExtent * y) + min;
        double maxX = minX + tileExtent;
        double maxY = minY + tileExtent;
        
        return new double[]{ minX, minY, maxX, maxY };
    }
    
    /**
     * According to the OGC Specification for WMS', a WMS endpoint must end in
     * either a ? or an &. In the wild, this is often not the case. This method
     * will check if the given wms url does terminate correctly. If not it will
     * append the correct character so that it does
     * @param url valid or invalid wms url
     * @return a valid wms url
     */
    protected String convertToValidWMSEndpoint(String url) {
        if(url.endsWith("?") || url.endsWith("&")) {
            return url; //The url is already valid
        }
        else if(url.contains("?")) {
            return url + "&";
        }
        else {
            return url + "?";
        }
    }
    
}
