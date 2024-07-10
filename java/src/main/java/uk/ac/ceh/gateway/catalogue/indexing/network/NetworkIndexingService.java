package uk.ac.ceh.gateway.catalogue.indexing.network;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The following NetworkIndexingService detects when a Facility record 'Belongs to'
 * a Network and will update that Network's bounding box if required.  The updated
 * Network is saved, which will trigger a DataSubmittedEvent and its bounding
 * box will be indexed in Solr.
 */

@Slf4j
@ToString
public class NetworkIndexingService {
    private final DocumentListingService listingService;
    private final BundledReaderService<MetadataDocument> bundledReader;
    private final DocumentRepository documentRepository;
    private final JenaLookupService lookupService;

    private final CatalogueUser user = new CatalogueUser("Network Indexing Service", "none@none.com");
    private final String commitMessage = "Network bounding box update triggered by change in component facility: %s";

    public NetworkIndexingService (
            DocumentListingService listingService,
            BundledReaderService<MetadataDocument> bundledReader,
            DocumentRepository documentRepository,
            JenaLookupService lookupService
    ){
        this.listingService = listingService;
        this.bundledReader = bundledReader;
        this.documentRepository = documentRepository;
        this.lookupService = lookupService;
    }

    /**
     * When a document is created or updated, if it is a monitoring facility, then the bounding box of any monitoring
     * network documents it belongs to may need their bounding box updated.
     * @param toIndex List of ids of documents that have been created or updated
     * @throws DocumentIndexingException
     */
    public void indexDocuments(List<String> toIndex) throws DocumentIndexingException {
        toIndex.stream().forEach(id -> {
            try {
                MetadataDocument document = bundledReader.readBundle(id);
                if(document instanceof MonitoringFacility facility) {
                    updateIndex(facility);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (PostProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * When a facility document is deleted, the bounding boxes of the Monitoring Networks it belongs to may need
     * updating.  This method expects to receive the list of documents that the deleted facility belonged to - this is
     * necessary since they can't be looked up now as the facility has gone. This method will update the bounding boxes
     * of these documents if they are monitoring networks.
     * @param belongsToIds List of ids of documents that the deleted facility belonged to
     */
    public void unindexDocuments(String facilityId, List<String> belongsToIds) throws DocumentIndexingException {
        belongsToIds.stream().forEach(id -> {
            try {
                MetadataDocument doc = bundledReader.readBundle(id);
                if(doc instanceof MonitoringNetwork networkDoc){
                    updateNetworkRemove(networkDoc, facilityId);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (PostProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
    /**
     * Pull out the parent networks of the facility and update their bounding box if required
     * @param facility The facility that has been created or edited
     */
    private void updateIndex(MonitoringFacility facility){
        facility.getRelationships().stream().forEach(r -> {
            if(isBelongsTo(r)) {
                try {
                    MetadataDocument doc = bundledReader.readBundle(r.getTarget());
                    if(doc instanceof MonitoringNetwork networkDoc) {
                        Optional<Geometry> facilityGeomOpt = Optional.ofNullable(facility.getGeometry());
                        if(facilityGeomOpt.isPresent() && facilityGeomOpt.get().getBoundingBox().isPresent()) {
                            updateNetworkAdd(networkDoc, facilityGeomOpt.get().getBoundingBox().get(), facility.getId());
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (PostProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * This will update the MonitoringNetwork's bounding box under the following conditions:
     * - if the MonitoringNetwork's bounding box is empty, it will be assigned that the facility's bbox
     * - if the facility's bbox is not completely inside the network's bbox, then the network bbox will be
     * set to the minimum bounding envelope of the intersection of both bboxes.
     * @param doc the metadata document for the MonitoringNetwork
     * @param facilityBbox the facility's bounding box
     */
    private void updateNetworkAdd(MonitoringNetwork doc, BoundingBox facilityBbox, String facilityId) {
        Optional<BoundingBox> networkBBoxOpt = Optional.ofNullable(doc.getBoundingBox());
        try {
            if(networkBBoxOpt.isEmpty()){
                doc.setBoundingBox(facilityBbox);
                documentRepository.save(user, doc, String.format(commitMessage, facilityId));
            } else if(!networkBBoxOpt.get().isSurrounding(facilityBbox)){
                doc.setBoundingBox(networkBBoxOpt.get().intersectingBBox(facilityBbox));
                documentRepository.save(user, doc, String.format(commitMessage, facilityId));
            }
        } catch (DocumentRepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find the child facilities for a network, excluding the facility that has been deleted already.  Generate
     * the bounding box for that collection of facilities and update the network's bounding box.
     * @param networkDoc the MetadataDocument of the network that needs updating
     * @param deletedFacilityId the string id of the deleted facility that should be excluded from the collection of
     * facilities that belong to the network
     */
    private void updateNetworkRemove(MonitoringNetwork networkDoc, String deletedFacilityId) {
        List<Link> linkedFacilities = lookupService.inverseRelationships(networkDoc.getUri(), Ontology.BELONGS_TO.getURI());
        List<Optional<BoundingBox>> extantFacilityBBoxes = linkedFacilities.stream()
            .filter(l -> !l.getHref().contains(deletedFacilityId)) //Make sure the deleted facility isn't included
            .map(l -> {
                return Geometry.builder()
                    .geometryString(l.getGeometry())
                    .build()
                    .getBoundingBox();
            })
            .collect(Collectors.toList());
        BoundingBox combinedBbox = null;
        for (Optional<BoundingBox> optVal : extantFacilityBBoxes) {
            if (optVal.isPresent()) {
                BoundingBox val = optVal.get();
                if (combinedBbox == null) {
                    combinedBbox = val;
                } else {
                    if (!combinedBbox.isSurrounding(val)) {
                        combinedBbox = combinedBbox.intersectingBBox(val);
                    }
                }
            }
        }
        networkDoc.setBoundingBox(combinedBbox);
        try {
            documentRepository.save(user, networkDoc, String.format(commitMessage, deletedFacilityId));
        } catch (DocumentRepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isBelongsTo(Relationship r) {
        return r.getRelation().equals(Ontology.BELONGS_TO.getURI());
    }
}
