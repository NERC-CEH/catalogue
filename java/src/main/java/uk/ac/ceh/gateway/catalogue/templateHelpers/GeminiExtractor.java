
package uk.ac.ceh.gateway.catalogue.templateHelpers;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The following service defines some methods which help in extracting details
 * from Gemini Documents and returning these as easy to use entities. Designed
 * to be freemarker friendly.
 */
@Slf4j
@ToString
@Service
public class GeminiExtractor {
    private final static Envelope GLOBAL_EXTENT = new Envelope(-180, 180, -90, 90);

    public GeminiExtractor() {
        log.info("Creating {}", this);
    }

    public List<String> getKeywords(GeminiDocument document) {
        return Optional.ofNullable(document.getDescriptiveKeywords())
                .orElse(Collections.emptyList())
                .stream()
                .map(DescriptiveKeywords::getKeywords)
                .flatMap(Collection::stream)
                .map(Keyword::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns the smallest extent which encompasses all of the bounding boxes
     *
     * @param document Gemini document to extract extent
     * @return smallest extent for the supplied bounding boxes
     * @throws com.vividsolutions.jts.io.ParseException if the wkt of the bbox
     *                                                  is incorrect
     */
    public Envelope getExtent(GeminiDocument document) throws ParseException {
        val possibleBBoxes = Optional.ofNullable(document.getBoundingBoxes());
        if (possibleBBoxes.isPresent()) {
            List<BoundingBox> clone = new ArrayList<>(document.getBoundingBoxes());
            if (!clone.isEmpty()) {
                WKTReader reader = new WKTReader();
                Geometry geo = reader.read(clone.remove(0).getWkt());
                for (BoundingBox bbox : clone) {
                    geo = geo.union(reader.read(bbox.getWkt()));
                }
                return geo.getEnvelopeInternal();
            } else {
                return GLOBAL_EXTENT;
            }
        } else {
            return GLOBAL_EXTENT;
        }
    }
}
