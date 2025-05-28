package uk.ac.ceh.gateway.catalogue.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import java.math.BigDecimal;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataciteRequest {
    @JsonProperty("data")
    Data data;

    public DataciteRequest(Map<String, Object> request, String url, JenaLookupService jenaLookupService, DataciteRequestService dataciteRequestService) {
        String doi = request.get("doi").toString();
        GeminiDocument document = (GeminiDocument) request.get("doc");
        String resourceType = Optional.ofNullable(request.get("resourceType"))
            .map(Object::toString)
            .orElse(null);
        this.data = new Data(doi, new Attributes(doi, document, url, resourceType, jenaLookupService, dataciteRequestService));
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        String id;
        String type = "dois";
        Attributes attributes;

        public Data(String id, Attributes attributes) {
            this.id = id;
            this.attributes = attributes;
        }
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {
        String doi;
        String event = "publish";
        String url;
        List<Title> titles;
        Types types;
        List<DataciteContact> creators;
        Publisher publisher;
        int publicationYear;
        List<DataciteContact> contributors;
        List<Subject> subjects;
        List<Date> dates;
        String language;
        List<Identifier> identifiers;
        List<RelatedIdentifier> relatedIdentifiers;
        List<String> formats;
        List<Rights> rightsList;
        List<Description> descriptions;
        List<GeoLocation> geoLocations;
        List<FundingReference> fundingReferences;

        public Attributes(String doi, GeminiDocument document, String url, String resourceType, JenaLookupService jenaLookupService, DataciteRequestService dataciteRequestService) {

            this.doi = doi;
            this.url = url;
            this.titles = List.of(new Title(document.getTitle()));
            this.types = new Types(resourceType, resourceType);
            this.creators = dataciteRequestService.dataciteContact(document, "creator");
            this.contributors = dataciteRequestService.dataciteContact(document, "contributor");
            this.publisher = dataciteRequestService.assignPublisher(document.getPublishers());
            this.publicationYear = dataciteRequestService.publicationDateCheck(document.getDatasetReferenceDate().getPublicationDate());
            this.subjects = dataciteRequestService.extractSubjects(document.getAllKeywords());
            this.dates = dataciteRequestService.setDateDetails(document.getDatasetReferenceDate());
            this.language = "en";
            this.identifiers = dataciteRequestService.getAlternateResourceIdentifiers(document.getResourceIdentifiers(), url);
            this.relatedIdentifiers = dataciteRequestService.createRelatedIdentifiers(document, jenaLookupService);
            this.formats = dataciteRequestService.gatherDistributionFormats(document.getDistributionFormats());
            this.rightsList = dataciteRequestService.listRights(document.getLicences());
            this.descriptions = dataciteRequestService.populateDescriptions(document);
            this.geoLocations = dataciteRequestService.extractGeoLocations(document.getBoundingBoxes());
            this.fundingReferences = dataciteRequestService.fundingDetails(document.getFunding());
        }

        public record DataciteContact(String contributorType, String name, String nameType, String givenName, String familyName, List<NameIdentifier> nameIdentifiers, List<Affiliation> affiliation) {}
        public record NameIdentifier(String nameIdentifier, String nameIdentifierScheme, String schemeUri) {}
        public record Affiliation(String name, String affiliationIdentifier, String affiliationIdentifierScheme, String schemeUri) {}
        public record Title(String title) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Types(String resourceType, String resourceTypeGeneral) {}
        public record Publisher(String name, String publisherIdentifier, String publisherIdentifierScheme, String schemeUri) {}
        public record Subject(String subject, String subjectScheme, String schemeUri, String valueUri, String classificationCode) {}
        public record Date(String date, String dateType, String dateInformation) {}
        public record Identifier(String identifier, String identifierType) {}
        public record RelatedIdentifier(String relatedIdentifier, String relatedIdentifierType, String relationType, String relatedMetadataScheme, String schemeUri, String schemeType, String resourceTypeGeneral) {}
        public record Rights(String rights, String lang, String rightsUri, String rightsIdentifier, String rightsIdentifierScheme, String schemeUri) {}
        public record Description(String description, String lang, String descriptionType) {}
        public record GeoLocation(GeoLocationBox geoLocationBox) {}
        public record GeoLocationBox(BigDecimal westBoundLongitude, BigDecimal eastBoundLongitude, BigDecimal southBoundLatitude, BigDecimal northBoundLatitude) {}
        public record FundingReference(String funderName, String funderIdentifier, String funderIdentifierType, String schemeUri, String awardNumber, String awardUri, String awardTitle) {}

    }
}
