package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.elter.ManufacturerDocument;
import uk.ac.ceh.gateway.catalogue.elter.SensorDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.ElterService;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

import java.util.List;

@Slf4j
@Controller
public class ElterController extends AbstractDocumentController {

  private ElterService elterService;

  @Autowired
  public ElterController(DocumentRepository documentRepository, ElterService elterService) {
    super(documentRepository);
    this.elterService = elterService;
  }

  // Sensor

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = ELTER_SENSOR_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newSensor(@ActiveUser CatalogueUser user, @RequestBody SensorDocument document, @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    setSensorManufacturer(document, user);
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Sensor Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = ELTER_SENSOR_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveSensor(@ActiveUser CatalogueUser user, @PathVariable("file") String file, @RequestBody SensorDocument document) throws DocumentRepositoryException {
    setSensorManufacturer(document, user);
    return saveMetadataDocument(user, file, document);
  }

  private void setSensorManufacturer(SensorDocument document, CatalogueUser user) {
    if (!document.getManufacturer().equalsIgnoreCase("other")) {
      val manufacturer = elterService.getManufacturer(document.getManufacturer());
      document.setManufacturerName(manufacturer.getTitle());
      document.setManufacturerWebsite(manufacturer.getWebsite());
    } else {
      val manufacturer = new ManufacturerDocument();
      manufacturer.setType("dataset");
      manufacturer.setTitle(document.getManufacturerName());
      manufacturer.setWebsite(document.getManufacturerWebsite());
      try {
        saveNewMetadataDocument(user, manufacturer, "elter", "new eLTER Manufacturer Document");
        document.setManufacturer(manufacturer.getId());
      } catch (DocumentRepositoryException err) {
        throw new RuntimeException(err);
      }
    }
  }

  @PreAuthorize("@permission.userCanView(#manufacturer)")
  @RequestMapping(value = "elter/sensors/{manufacturer}", method = RequestMethod.GET)
  @ResponseBody
  public List<SensorDocument> getSensorsFor(@ActiveUser CatalogueUser user, @PathVariable("manufacturer") String manufacturer) throws DocumentRepositoryException {
    return this.elterService.getSensors(manufacturer);
  }

  // Manufacturer

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = ELTER_MANUFACTURER_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newManufacturer(@ActiveUser CatalogueUser user, @RequestBody ManufacturerDocument document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new eLTER Manufacturer Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = ELTER_MANUFACTURER_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveManufacturer(@ActiveUser CatalogueUser user, @PathVariable("file") String file, @RequestBody ManufacturerDocument document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }

  @PreAuthorize("@permission.userCanCreate('elter')")
  @RequestMapping(value = "elter/manufacturers", method = RequestMethod.GET)
  @ResponseBody
  public List<ManufacturerDocument> getManufacturers(@ActiveUser CatalogueUser user) throws DocumentRepositoryException {
    return this.elterService.getManufacturers();
  }
}