package uk.ac.ceh.gateway.catalogue.indexing.network;

import lombok.SneakyThrows;
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
    public static final String COMMIT_MESSAGE_TEMPLATE = "Network bounding box update triggered by change in component facility: %s";

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
    @SneakyThrows
    public void indexDocuments(List<String> toIndex) throws DocumentIndexingException {
        toIndex.stream().forEach(id -> {
            try {
                MetadataDocument document = bundledReader.readBundle(id);
                if(document instanceof MonitoringFacility facility) {
                    updateRelatedNetworks(facility);
                }
            } catch (IOException | PostProcessingException e) {
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
                    updateBoundingBox(networkDoc, Optional.empty(), Optional.of(facilityId), facilityId);
                }
            } catch (IOException | PostProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Pull out the parent networks of the facility and update their bounding box if required
     * @param facility The facility that has been created or edited
     */
    private void updateRelatedNetworks(MonitoringFacility facility){
        facility.getRelationships().stream()
            .filter(this::isBelongsTo)
            .forEach(r -> {
                try {
                    MetadataDocument doc = bundledReader.readBundle(r.getTarget());
                    if(doc instanceof MonitoringNetwork networkDoc) {
                        this.updateBoundingBox(networkDoc, Optional.of(facility.getId()), Optional.empty(), facility.getId());
                    }
                } catch (IOException | PostProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * This uses the Jena lookup service to find all the facilities that belong to a network in order to rebuild the
     * envelope of the network.  There is a possibility that the Jena service may not have been updated with the change
     * being processed, so the list of facilities returned by the inverseRelationship lookup is checked for either the
     * presence or abscence of the facility we already know about and adjusted accordingly.
     * @param networkDoc the network whose bounding box will be re-generated
     * @param mustIncludeFacility a facility that belongs to the network as it has just been added and must be present
     *                            in the list
     * @param mustExcludeFacility a facility that does not belong in the network because it has just been removed and
     *                            must not be present in the list
     */
    @SneakyThrows
    private void updateBoundingBox(MonitoringNetwork networkDoc, Optional<String> mustIncludeFacility, Optional<String> mustExcludeFacility, String intiatingFacility){
        List<Link> linkedFacilities = lookupService.inverseRelationships(networkDoc.getUri(), Ontology.BELONGS_TO.getURI());
        List<BoundingBox> extantFacilityBBoxes = getBboxesWithoutExcluded(linkedFacilities, mustExcludeFacility);
        addFacility(linkedFacilities, extantFacilityBBoxes, mustIncludeFacility);
        Optional<BoundingBox> combinedBbox = getEnvelope(extantFacilityBBoxes);
        // Update the network
        networkDoc.setBoundingBox(combinedBbox.orElse(null));
        try {
            documentRepository.save(user, networkDoc, String.format(COMMIT_MESSAGE_TEMPLATE, intiatingFacility));
        } catch (DocumentRepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a list of bounding boxes for the linkedFacilities and remove the one identified in mustExcludeFacility,
     * which may or may not be in the list
     * @param linkedFacilities the list of links that contain info needed for each bounding box
     * @param mustExcludeFacility the id of a facility that must not occur in the list
     */
    private List<BoundingBox> getBboxesWithoutExcluded(List<Link> linkedFacilities, Optional<String> mustExcludeFacility) {
        List<BoundingBox> extantFacilityBBoxes = linkedFacilities.stream()
            .filter(l -> mustExcludeFacility.isEmpty() || !l.getHref().contains(mustExcludeFacility.get())) //Make sure deleted facility, or facility with removed relationship isn't included
            .flatMap(l -> {
                return Geometry.builder()
                    .geometryString(l.getGeometry())
                    .build()
                    .getBoundingBox().stream();
            })
            .toList();
        return extantFacilityBBoxes;
    }


    @SneakyThrows
    private void addFacility(List<Link> linkedFacilities, List<BoundingBox> extantFacilityBBoxes, Optional<String> mustIncludeFacility) {
        if(mustIncludeFacility.isPresent()) {
            if (linkedFacilities.stream().noneMatch(l -> l.getHref().contains(mustIncludeFacility.get()))) {
                extantFacilityBBoxes.add(
                    ((MonitoringFacility) bundledReader.readBundle(mustIncludeFacility.get())).getGeometry().getBoundingBox().get()
                );
            }
        }
    }

    /**
     * Get the envelope of all the component facility envelopes
     * @param extantFacilityBboxes a list of bounding boxes to get combined envelope from
     */
    protected Optional<BoundingBox> getEnvelope(List<BoundingBox> extantFacilityBboxes) {
        return extantFacilityBboxes.stream()
            .reduce(BoundingBox::envelope);
    }

    protected boolean isBelongsTo(Relationship r) {
        return (r.getRelation() != null) && r.getRelation().equals(Ontology.BELONGS_TO.getURI());
    }
}
