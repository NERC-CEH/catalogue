package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.osdp.*;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Controller
public class OsdpDocumentController extends AbstractDocumentController {

    @Autowired
    public OsdpDocumentController(DocumentRepository documentRepository) {
        super(documentRepository);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_AGENT_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newAgent(
        @ActiveUser CatalogueUser user,
        @RequestBody Agent document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Agent"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_AGENT_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateAgent(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody Agent document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_DATASET_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newDataset(
        @ActiveUser CatalogueUser user,
        @RequestBody Dataset document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Dataset"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_DATASET_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateDataset(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody Dataset document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_LOCATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newLocation(
        @ActiveUser CatalogueUser user,
        @RequestBody Location document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Location"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_LOCATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateLocation(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody Location document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newModel(
        @ActiveUser CatalogueUser user,
        @RequestBody Model document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Model"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateModel(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody Model document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_MONITORING_ACTIVITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newMonitoringActivity(
        @ActiveUser CatalogueUser user,
        @RequestBody MonitoringActivity document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Monitoring Activity"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_MONITORING_ACTIVITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateMonitoringActivity(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody MonitoringActivity document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_MONITORING_FACILITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newMonitoringFacility(
        @ActiveUser CatalogueUser user,
        @RequestBody MonitoringFacility document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Monitoring Facility"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_MONITORING_FACILITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateMonitoringFacility(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody MonitoringFacility document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_MONITORING_PROGRAMME_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newMonitoringProgramme(
        @ActiveUser CatalogueUser user,
        @RequestBody MonitoringProgramme document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Monitoring Programme"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_MONITORING_PROGRAMME_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateMonitoringProgramme(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody MonitoringProgramme document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_PARAMETER_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newParameter(
        @ActiveUser CatalogueUser user,
        @RequestBody Parameter document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Parameter"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_PARAMETER_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateParameter(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody Parameter document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_PUBLICATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newPublication(
        @ActiveUser CatalogueUser user,
        @RequestBody Publication document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Publication"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_PUBLICATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updatePublication(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody Publication document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = OSDP_SAMPLE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newSample(
        @ActiveUser CatalogueUser user,
        @RequestBody Sample document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new OSDP Sample"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = OSDP_SAMPLE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateSample(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody Sample document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }
}
