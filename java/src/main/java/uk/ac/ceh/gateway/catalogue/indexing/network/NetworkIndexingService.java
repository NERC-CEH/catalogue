package uk.ac.ceh.gateway.catalogue.indexing.network;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.reasoner.rulesys.builtins.Bound;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.ef.Link;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The following NetworkIndexingService detects when a Facility record 'Belongs to'
 * a Network and will update that Network's bounding box if required.  The updated
 * Network is saved, which will trigger a DataSubmittedEvent and its bounding
 * box will be indexed in Solr.
 */

@Slf4j
@ToString
public class NetworkIndexingService implements DocumentIndexingService {
    private final BundledReaderService<MetadataDocument> bundledReader;

    public NetworkIndexingService (BundledReaderService<MetadataDocument> bundledReader){
        this.bundledReader = bundledReader;
    }

    @Override
    public void indexDocuments(List<String> toIndex, String revision) throws DocumentIndexingException {
        System.out.println("Doing some indexing");
        for(String metadataId: toIndex) {
            System.out.println(String.format("Indexing: %s", metadataId));
            try {
                MetadataDocument document = bundledReader.readBundle(metadataId, revision);
                System.out.println(document.getType());
                System.out.println(document.getClass().getName());
                if (document instanceof MonitoringFacility facility) {
                    indexDocument(facility);
                }
            }
            catch(Exception ex) {
                log.error("Failed to read metadata document", ex);
            }
        }
    }

    private void indexDocument(MonitoringFacility facility) {
        System.out.println("looking for relationships");
        facility.getRelationships().stream().forEach(r -> {
            if(isBelongsTo(r)) {
                try {
                    MetadataDocument doc = bundledReader.readBundle(r.getTarget());
                    if(doc instanceof MonitoringNetwork){
                        Optional<Geometry> facilityGeomOpt = Optional.of(facility.getGeometry());
                        if(facilityGeomOpt.isPresent()){
                            updateNetwork((MonitoringNetwork)doc, facilityGeomOpt.get().getBoundingBox());
                        }
                    } else {
                        System.out.println("not a network");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (PostProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        System.out.println("Done looking for relationships");
    }

    /**
     * This will update the MonitoringNetwork's bounding box under the following conditions:
     * - if the MonitoringNetwork's bounding box is empty, it will get that the facility's bbox if it has one
     * - if the facility has a bbox and it is not completely inside the network's bbox, then the network bbox will be set to
     * the minimum envelope of the intersection of both bboxes.
     * @param doc the metadata document for the MonitoringNetwork
     * @param facilityBbox an Optional for the facility's bounding box
     */
    private void updateNetwork(MonitoringNetwork doc, Optional<BoundingBox> facilityBbox) {
        if(facilityBbox.isPresent()){
            Optional<BoundingBox> networkBBoxOpt = Optional.ofNullable(doc.getBoundingBox());
            if(networkBBoxOpt.isEmpty()){
                doc.setBoundingBox(facilityBbox.get());
                //SAVE
            } else if(networkBBoxOpt.get().isSurrounding(facilityBbox.get())){
                doc.setBoundingBox(networkBBoxOpt.get().intersectingBBox(facilityBbox.get()));
                //SAVE
            } else {
                return;
            }
        }
    }

    protected boolean isBelongsTo(Relationship r) {
        return r.getRelation().equals(Ontology.BELONGS_TO.getURI());
    }

    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return false;
    }

    @Override
    public void rebuildIndex() throws DocumentIndexingException {

    }

    @Override
    public void unindexDocuments(List<String> unIndex) throws DocumentIndexingException {

    }

    @Override
    public void attemptIndexing() {

    }

}
