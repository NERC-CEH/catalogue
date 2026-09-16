package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.geometry.GraticuleCell;

/**
 * Reduces a confidential facility's geometry to the graticule cell containing it,
 * so that the precise location is never persisted in the current revision of the
 * document, and therefore never reaches Solr, Jena, Fuseki, a template or the API.
 *
 * <p>This runs on the save path deliberately. The equivalent transform in the
 * browser was bypassed by the raw-JSON geometry editor, by any direct API PUT, and
 * by every geometry type the client did not handle - which was all of them except
 * Point. Doing it once, server side, means every downstream consumer reads an
 * already-safe geometry and needs no guard of its own.
 *
 * <p>The transform is total: any geometry with a bounding box reduces to a cell,
 * anywhere in the world. There is no input for which a confidential location is
 * left precise, and no failure mode in which a save is refused.
 */
@Slf4j
@Service
public class LocationObfuscationService {

    /**
     * Snaps the document's geometry to a graticule cell if it is marked confidential.
     *
     * <p>Safe to call on every save: snapping the centroid of a cell yields that same
     * cell, so a document that has already been obfuscated is left unchanged.
     *
     * @param document the document about to be saved
     * @return the same document, with its geometry replaced where required
     */
    public MonitoringFacility obfuscate(MonitoringFacility document) {
        Geometry geometry = document.getGeometry();
        if (geometry == null || !Boolean.TRUE.equals(geometry.getLocationConfidential())) {
            return document;
        }

        // A blank geometryString yields an empty bounding box, so this covers it too.
        geometry.getBoundingBox().ifPresent(boundingBox -> {
            GraticuleCell cell = GraticuleCell.covering(boundingBox);

            log.info(
                "Obfuscating confidential geometry of {} to the cell at {}, {}",
                document.getId(), cell.minLongitude(), cell.minLatitude()
            );

            document.setGeometry(Geometry.builder()
                .geometryString(cell.toGeoJson())
                .locationConfidential(true)
                .build());
        });

        return document;
    }
}
