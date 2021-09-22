package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.gemini.Service;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)

public class ServiceAgreement extends AbstractMetadataDocument {
    private List<ResponsibleParty> distributorContacts, responsibleParties;
    private String depositorName;
    private String depositReference;
    private String dataIdentifier;
    private Service service;
    private String DOI;
    private Number dataFiles;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private List<String> fileNames;
    private List<String> fileFormats;
    private Number fileSize;
    private String transferMethod;
    private List<RelatedRecord> relatedRecords;
    private String dataCategory;
    private String availability;
    private String specificRequirements;
    private String otherServicesRequired;
    private String endUserLicence;
    private String ownerOfIpr;
    private Depositor depositor;
    private Depositor forTheEIDC;
    //Superseding
    private String superSeededMetadataId;
    private String reason;
    //Discovery metadata
    private String description;
    private String lineage;
    private List<Keyword> keywords;
    private String areaOfStudy;
    private List<Document> documents;
    private List<String> userConstraints;


    public List<ResponsibleParty> getResponsibleParties() {
        return Optional.ofNullable(responsibleParties)
                .orElse(Collections.emptyList());
    }

    public List<ResponsibleParty> getAuthors() {
        return Optional.ofNullable(responsibleParties)
                .orElse(Collections.emptyList())
                .stream()
                .filter((authors) -> authors.getRole().equalsIgnoreCase("author"))
                .collect(Collectors.toList());
    }

    public List<String> getCoupledResources() {
        return Optional.ofNullable(service)
                .map(Service::getCoupledResources)
                .orElse(Collections.emptyList())
                .stream()
                .map(Service.CoupledResource::getIdentifier)
                .filter(cr -> !cr.isEmpty())
                .collect(Collectors.toList());
    }
}
