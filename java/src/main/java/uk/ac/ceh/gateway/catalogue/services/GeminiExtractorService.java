
package uk.ac.ceh.gateway.catalogue.services;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

/**
 * The following service defines some methods which help in extracting details
 * from Gemini Documents and returning these as easy to use entities. Designed
 * to be freemarker friendly.
 * @author cjohn
 */
@AllArgsConstructor
public class GeminiExtractorService {    
    public List<String> getKeywords(List<DescriptiveKeywords> keywords) {
        return keywords.stream()
                .filter((dk) -> dk.getThesaurusName() == null)
                .map(DescriptiveKeywords::getKeywords)
                .flatMap((k) -> k.stream())
                .map(Keyword::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the smallest extent which encompasses all of the bounding boxes
     * @param boundingBoxes
     * @return smallest extent for the supplied bounding boxes
     * @throws com.vividsolutions.jts.io.ParseException if the wkt of the bbox
     * is incorrect
     */
    public Envelope getExtent(List<BoundingBox> boundingBoxes) throws ParseException {
        List<BoundingBox> clone = new ArrayList<>(boundingBoxes);
        if(!clone.isEmpty()) {
            WKTReader reader = new WKTReader();
            Geometry geo = reader.read(clone.remove(0).getWkt());
            for(BoundingBox bbox: clone) {
                geo = geo.union(reader.read(bbox.getWkt()));
            }
            return geo.getEnvelopeInternal();
        }
        else {
            return null;
        }
    }
}
