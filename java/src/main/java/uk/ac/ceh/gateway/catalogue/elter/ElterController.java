package uk.ac.ceh.gateway.catalogue.elter;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.AbstractDocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Controller
public class ElterController extends AbstractDocumentController {
    public static final String MAINTENANCE_ROLE = "ROLE_CIG_SYSTEM_ADMIN";

    @Autowired
    public ElterController(DocumentRepository documentRepository) {
        super(documentRepository);
    }

    List<Class> classes = Arrays.asList(CompositeFeature.class, Condition.class, DeploymentRelatedProcessDuration.class,
            Input.class, Manufacturer.class, MonitoringFeature.class, ObservableProperty.class,
            ObservationPlaceholder.class, OperatingPropertyProperty.class, OperatingRange.class, Person.class,
            SampleFeature.class, Sensor.class, SensorType.class, SingleSystemDeployment.class, Stimulus.class,
            SystemCapability.class, SystemProperty.class, TemporalProcedure.class, VerticalMonitoringFeature.class);

    @RequestMapping(value = "/elter/create", method = RequestMethod.GET)
    public ResponseEntity<List<Class>> create(@ActiveUser CatalogueUser user) {
        return ResponseEntity.ok(classes);
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.CompositeFeature+json")
    public ResponseEntity<MetadataDocument> updateCompositeFeature(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody CompositeFeature document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.CompositeFeature+json")
    public ResponseEntity<MetadataDocument> saveNewCompositeFeature(@ActiveUser CatalogueUser user,
            @RequestBody CompositeFeature document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new CompositeFeature");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.Condition+json")
    public ResponseEntity<MetadataDocument> updateCondition(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody Condition document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.Condition+json")
    public ResponseEntity<MetadataDocument> saveNewCondition(@ActiveUser CatalogueUser user,
            @RequestBody Condition document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new Condition");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.DeploymentRelatedProcessDuration+json")
    public ResponseEntity<MetadataDocument> updateDeploymentRelatedProcessDuration(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody DeploymentRelatedProcessDuration document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.DeploymentRelatedProcessDuration+json")
    public ResponseEntity<MetadataDocument> saveNewDeploymentRelatedProcessDuration(@ActiveUser CatalogueUser user,
            @RequestBody DeploymentRelatedProcessDuration document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new DeploymentRelatedProcessDuration");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.Input+json")
    public ResponseEntity<MetadataDocument> updateInput(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody Input document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.Input+json")
    public ResponseEntity<MetadataDocument> saveNewInput(@ActiveUser CatalogueUser user, @RequestBody Input document,
            @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new Input");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.Manufacturer+json")
    public ResponseEntity<MetadataDocument> updateManufacturer(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody Manufacturer document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.Manufacturer+json")
    public ResponseEntity<MetadataDocument> saveNewManufacturer(@ActiveUser CatalogueUser user,
            @RequestBody Manufacturer document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new Manufacturer");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.MonitoringFeature+json")
    public ResponseEntity<MetadataDocument> updateMonitoringFeature(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody MonitoringFeature document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.MonitoringFeature+json")
    public ResponseEntity<MetadataDocument> saveNewMonitoringFeature(@ActiveUser CatalogueUser user,
            @RequestBody MonitoringFeature document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new MonitoringFeature");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.ObservableProperty+json")
    public ResponseEntity<MetadataDocument> updateObservableProperty(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody ObservableProperty document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.ObservableProperty+json")
    public ResponseEntity<MetadataDocument> saveNewObservableProperty(@ActiveUser CatalogueUser user,
            @RequestBody ObservableProperty document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new ObservableProperty");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.ObservationPlaceholder+json")
    public ResponseEntity<MetadataDocument> updateObservationPlaceholder(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody ObservationPlaceholder document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.ObservationPlaceholder+json")
    public ResponseEntity<MetadataDocument> saveNewObservationPlaceholder(@ActiveUser CatalogueUser user,
            @RequestBody ObservationPlaceholder document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new ObservationPlaceholder");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.OperatingRange+json")
    public ResponseEntity<MetadataDocument> updateOperatingRange(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody OperatingRange document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.OperatingRange+json")
    public ResponseEntity<MetadataDocument> saveNewOperatingRange(@ActiveUser CatalogueUser user,
            @RequestBody OperatingRange document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new OperatingRange");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.Person+json")
    public ResponseEntity<MetadataDocument> updatePerson(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody Person document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.Person+json")
    public ResponseEntity<MetadataDocument> saveNewPerson(@ActiveUser CatalogueUser user, @RequestBody Person document,
            @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new Person");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.SampleFeature+json")
    public ResponseEntity<MetadataDocument> updateSampleFeature(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody SampleFeature document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.SampleFeature+json")
    public ResponseEntity<MetadataDocument> saveNewSampleFeature(@ActiveUser CatalogueUser user,
            @RequestBody SampleFeature document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new SampleFeature");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.Sensor+json")
    public ResponseEntity<MetadataDocument> updateSensor(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody Sensor document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.Sensor+json")
    public ResponseEntity<MetadataDocument> saveNewSensor(@ActiveUser CatalogueUser user, @RequestBody Sensor document,
            @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new Sensor");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.SensorType+json")
    public ResponseEntity<MetadataDocument> updateSensorType(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody SensorType document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.SensorType+json")
    public ResponseEntity<MetadataDocument> saveNewSensorType(@ActiveUser CatalogueUser user,
            @RequestBody SensorType document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new SensorType");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.SingleSystemDeployment+json")
    public ResponseEntity<MetadataDocument> updateSingleSystemDeployment(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody SingleSystemDeployment document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.SingleSystemDeployment+json")
    public ResponseEntity<MetadataDocument> saveNewSingleSystemDeployment(@ActiveUser CatalogueUser user,
            @RequestBody SingleSystemDeployment document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new SingleSystemDeployment");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.Stimulus+json")
    public ResponseEntity<MetadataDocument> updateStimulus(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody Stimulus document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.Stimulus+json")
    public ResponseEntity<MetadataDocument> saveNewStimulus(@ActiveUser CatalogueUser user,
            @RequestBody Stimulus document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new Stimulus");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.SystemCapability+json")
    public ResponseEntity<MetadataDocument> updateSystemCapability(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody SystemCapability document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.SystemCapability+json")
    public ResponseEntity<MetadataDocument> saveNewSystemCapability(@ActiveUser CatalogueUser user,
            @RequestBody SystemCapability document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new SystemCapability");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.SystemProperty+json")
    public ResponseEntity<MetadataDocument> updateSystemProperty(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody SystemProperty document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.SystemProperty+json")
    public ResponseEntity<MetadataDocument> saveNewSystemProperty(@ActiveUser CatalogueUser user,
            @RequestBody SystemProperty document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new SystemProperty");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.TemporalProcedure+json")
    public ResponseEntity<MetadataDocument> updateTemporalProcedure(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody TemporalProcedure document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.TemporalProcedure+json")
    public ResponseEntity<MetadataDocument> saveNewTemporalProcedure(@ActiveUser CatalogueUser user,
            @RequestBody TemporalProcedure document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new TemporalProcedure");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = "application/vnd.VerticalMonitoringFeature+json")
    public ResponseEntity<MetadataDocument> updateVerticalMonitoringFeature(@ActiveUser CatalogueUser user,
            @PathVariable("file") String file, @RequestBody VerticalMonitoringFeature document)
            throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = "application/vnd.VerticalMonitoringFeature+json")
    public ResponseEntity<MetadataDocument> saveNewVerticalMonitoringFeature(@ActiveUser CatalogueUser user,
            @RequestBody VerticalMonitoringFeature document, @RequestParam("catalogue") String catalogue)
            throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new VerticalMonitoringFeature");
    }
}