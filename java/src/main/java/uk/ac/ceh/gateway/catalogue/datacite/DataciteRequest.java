package uk.ac.ceh.gateway.catalogue.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataciteRequest {
    @JsonProperty("data")
    Data data;

    public DataciteRequest(Map<String, Object> request, String url, JenaLookupService jenaLookupService) {
        String doi = request.get("doi").toString();
        GeminiDocument document = (GeminiDocument) request.get("doc");
        String resourceType = Optional.ofNullable(request.get("resourceType"))
            .map(Object::toString)
            .orElse(null);
        this.data = new Data(doi, new Attributes(doi, document, url, resourceType, jenaLookupService));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonProperty("id")
        String id;
        @JsonProperty("type")
        String type = "dois";
        @JsonProperty("attributes")
        Attributes attributes;

        public Data(String id, Attributes attributes) {
            this.id = id;
            this.attributes = attributes;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {

        public Attributes(String doi, GeminiDocument document, String url, String resourceType, JenaLookupService jenaLookupService) {
            this.doi = doi;
            this.url = url;
            this.titles = List.of(new Title(document.getTitle()));
            this.types = new Types(resourceType, resourceType);
            this.creators = dataciteContact(document, "creator");
            this.contributors = dataciteContact(document, "contributor");
            this.publisher = assignPublisher(document.getPublishers());
            this.publicationYear = publicationDateCheck(document.getDatasetReferenceDate().getPublicationDate());
            this.subjects = extractSubjects(document.getAllKeywords());
            this.dates = setDateDetails(document.getDatasetReferenceDate());
            this.language = "en";
            this.identifiers = getAlternateResourceIdentifiers(document.getResourceIdentifiers());
            this.relatedIdentifiers = createRelatedIdentifiers(document, jenaLookupService);
            this.formats = gatherDistributionFormats(document.getDistributionFormats());
            this.rightsList = listRights(document.getLicences());
            this.descriptions = populateDescriptions(document);
            this.geoLocations = extractGeoLocations(document.getBoundingBoxes());
            this.fundingReferences = fundingDetails(document.getFunding());
        }

        @JsonProperty("doi")
        String doi;
        @JsonProperty("event")
        String event = "publish";
        @JsonProperty("url")
        String url;
        @JsonProperty("titles")
        List<Title> titles;
        @JsonProperty("types")
        Types types;
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonProperty("creators")
        List<DataciteContact> creators;
        @JsonProperty("publisher")
        Publisher publisher;
        @JsonProperty("publicationYear")
        int publicationYear;
        @JsonProperty("contributors")
        List<DataciteContact> contributors;
        @JsonProperty("subjects")
        List<Subject> subjects;
        @JsonProperty("dates")
        List<Date> dates;
        @JsonProperty("language")
        String language;
        @JsonProperty("identifiers")
        List<Identifier> identifiers;
        @JsonProperty("relatedIdentifiers")
        List<RelatedIdentifier> relatedIdentifiers;
        @JsonProperty("formats")
        List<String> formats;
        @JsonProperty("rightsList")
        List<Rights> rightsList;
        @JsonProperty("descriptions")
        List<Description> descriptions;
        @JsonProperty("geoLocations")
        List<GeoLocation> geoLocations;
        @JsonProperty("fundingReferences")
        List<FundingReference> fundingReferences;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DataciteContact {
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonProperty("contributorType")
            String contributorType;
            @JsonProperty("name")
            String name;
            @JsonProperty("nameType")
            String nameType;
            @JsonProperty("givenName")
            String givenName;
            @JsonProperty("familyName")
            String familyName;
            @JsonProperty("nameIdentifiers")
            List<NameIdentifier> nameIdentifiers;
            @JsonProperty("affiliation")
            List<Affiliation> affiliation;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class NameIdentifier {
            @JsonProperty("nameIdentifier")
            String nameIdentifier;
            @JsonProperty("nameIdentifierScheme")
            String nameIdentifierScheme;
            @JsonProperty("schemeUri")
            String schemeUri;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Affiliation {
            @JsonProperty("name")
            String name;
            @JsonProperty("affiliationIdentifier")
            String affiliationIdentifier;
            @JsonProperty("affiliationIdentifierScheme")
            String affiliationIdentifierScheme;
            @JsonProperty("schemeUri")
            String schemeUri;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Title {
            @JsonProperty("title")
            String title;
        }
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Types {
            @JsonProperty("resourceType")
            String resourceType;
            @JsonProperty("resourceTypeGeneral")
            String resourceTypeGeneral;
        }
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Publisher {
            @JsonProperty("name")
            String name;
            @JsonProperty("publisherIdentifier")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String publisherIdentifier;
            @JsonProperty("publisherIdentifierScheme")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String publisherIdentifierScheme;
            @JsonProperty("schemeUri")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String schemeUri;

            public Publisher(String name) {
                this.name = name;
                this.publisherIdentifier = null;
                this.publisherIdentifierScheme = null;
                this.schemeUri = null;
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Subject {
            @JsonProperty("subject")
            String subject;
            @JsonProperty("subjectScheme")
            String subjectScheme;
            @JsonProperty("schemeUri")
            String schemeUri;
            @JsonProperty("valueUri")
            String valueUri;
            @JsonProperty("classificationCode")
            String classificationCode;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Date {
            @JsonProperty("date")
            String date;
            @JsonProperty("dateType")
            String dateType;
            @JsonProperty("dateInformation")
            String dateInformation;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Identifier {
            @JsonProperty("identifier")
            String identifier;
            @JsonProperty("identifierType")
            String identifierType;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RelatedIdentifier {
            @JsonProperty("relatedIdentifier")
            String relatedIdentifier;
            @JsonProperty("relatedIdentifierType")
            String relatedIdentifierType;
            @JsonProperty("relationType")
            String relationType;
            @JsonProperty("relatedMetadataScheme")
            String relatedMetadataScheme;
            @JsonProperty("schemeUri")
            String schemeUri;
            @JsonProperty("schemeType")
            String schemeType;
            @JsonProperty("resourceTypeGeneral")
            String resourceTypeGeneral;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Rights {
            @JsonProperty("rights")
            String rights;
            @JsonProperty("lang")
            String lang;
            @JsonProperty("rightsUri")
            String rightsUri;
            @JsonProperty("rightsIdentifier")
            String rightsIdentifier;
            @JsonProperty("rightsIdentifierScheme")
            String rightsIdentifierScheme;
            @JsonProperty("schemeUri")
            String schemeUri;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Description {
            @JsonProperty("description")
            String description;
            @JsonProperty("lang")
            String lang;
            @JsonProperty("descriptionType")
            String descriptionType;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GeoLocation {
            @JsonProperty("geoLocationBox")
            GeoLocationBox geoLocationBox;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GeoLocationBox {
            @JsonProperty("westBoundLongitude")
            BigDecimal westBoundLongitude;
            @JsonProperty("eastBoundLongitude")
            BigDecimal eastBoundLongitude;
            @JsonProperty("southBoundLatitude")
            BigDecimal southBoundLatitude;
            @JsonProperty("northBoundLatitude")
            BigDecimal northBoundLatitude;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class FundingReference {
            @JsonProperty("funderName")
            String funderName;
            @JsonProperty("funderIdentifier")
            String funderIdentifier;
            @JsonProperty("funderIdentifierType")
            String funderIdentifierType;
            @JsonProperty("schemeUri")
            String schemeUri;
            @JsonProperty("awardNumber")
            String awardNumber;
            @JsonProperty("awardUri")
            String awardUri;
            @JsonProperty("awardTitle")
            String awardTitle;
        }

        public List<FundingReference> fundingDetails(List<Funding> funders) {
            return funders.stream()
                .map(funder -> {
                    String funderIdentifier = funder.getFunderIdentifier();
                    String funderIdentifierType = null;

                    if (funderIdentifier != null && !funderIdentifier.isBlank()) {
                        if (funder.isRor()) {
                            funderIdentifierType = "ROR";
                        } else if (funder.isOrcid()) {
                            funderIdentifierType = "Crossref Funder";
                        } else {
                            funderIdentifierType = "Other";
                        }
                    }

                    return new FundingReference(
                        funder.getFunderName(),
                        funderIdentifier,
                        funderIdentifierType,
                        "",
                        funder.getAwardNumber(),
                        "",
                        funder.getAwardTitle()
                    );
                })
                .toList();
        }

        public List<GeoLocation> extractGeoLocations(List<BoundingBox> boundingBoxes) {
            List<GeoLocation> geoLocations = new ArrayList<>();

            for (BoundingBox boundingBox : boundingBoxes) {
                GeoLocationBox box = new GeoLocationBox(
                    boundingBox.getWestBoundLongitude(),
                    boundingBox.getEastBoundLongitude(),
                    boundingBox.getSouthBoundLatitude(),
                    boundingBox.getNorthBoundLatitude()
                );
                geoLocations.add(new GeoLocation(box));
            }
            return geoLocations;
        }

        public List<Description> populateDescriptions(GeminiDocument document) {
            return Stream.of(
                    new Description(document.getDescription(), "", "Abstract"),
                    (document.getLineage() != null && !document.getLineage().isEmpty())
                        ? new Description(document.getLineage(), "", "Methods")
                        : null
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        public List<Rights> listRights(List<ResourceConstraint> licences) {
            return licences.stream()
                .map(resourceConstraint -> {
                    String value = resourceConstraint.getValue();
                    String uri = resourceConstraint.getUri();
                    boolean isLicense = "license".equals(resourceConstraint.getCode()) && !uri.isEmpty();

                    if (isLicense) {
                        if ("https://eidc.ceh.ac.uk/licences/OGL/plain".equals(uri)) {
                            return new Rights(value, "", uri, "OGL-UK-3.0", "SPDX", "");
                        }
                        return new Rights(value, "", uri, "", "", "");
                    } else {
                        return new Rights(value, "", "", "", "", "");
                    }
                })
                .collect(Collectors.toList());
        }

        public List<String> gatherDistributionFormats(List<DistributionInfo> distributionFormats) {
            return distributionFormats.stream()
                .map(distributionInfo -> Stream.of(distributionInfo.getType(), distributionInfo.getName())
                    .filter(string -> string != null && !string.isEmpty())
                    .collect(Collectors.joining(" ")))
                .collect(Collectors.toList());
        }

        public List<RelatedIdentifier> createRelatedIdentifiers(GeminiDocument document, JenaLookupService jenaLookupService) {
            List<RelatedIdentifier> relatedIdentifiers = new ArrayList<>();

            List<OnlineResource> infoLinks = document.getInfoLinks() != null
                ? document.getInfoLinks()
                : List.of();
            List<OnlineResource> filteredOnlineResources = infoLinks.isEmpty()
                ? List.of()
                : filteredOnlineResources(infoLinks);
            List<Link> relSupersedes = jenaLookupService.relationships(
                document.getUri(),
                "https://vocabs.ceh.ac.uk/eidc#supersedes"
            );
            List<Link> relSupersedesBy = jenaLookupService.inverseRelationships(
                document.getUri(),
                "https://vocabs.ceh.ac.uk/eidc#supersedes"
            );

            for (OnlineResource resource : filteredOnlineResources) {
                relatedIdentifiers.add(new RelatedIdentifier(
                    resource.getUrl(), "URL", "IsDescribedBy", "", "", "", "Text"
                ));
            }
            for (Link link : relSupersedes) {
                relatedIdentifiers.add(new RelatedIdentifier(
                    link.getHref().replace("https://catalogue.ceh.ac.uk/id/", "10.5285/"),
                    "DOI", "IsNewVersionOf", "", "", "", "Dataset"
                ));
            }
            for (Link link : relSupersedesBy) {
                relatedIdentifiers.add(new RelatedIdentifier(
                    link.getHref().replace("https://catalogue.ceh.ac.uk/id/", "10.5285/"),
                    "DOI", "IsPreviousVersionOf", "", "", "", "Dataset"
                ));
            }
            for (Supplemental supplemental : Optional.ofNullable(document.getIncomingCitations()).orElse(List.of())) {
                String url = supplemental.getUrl();
                boolean isDoi = url.matches("^http(s)?://(dx\\.)?doi.org/10\\.\\d{2,9}/.+$");
                String idType = isDoi ? "DOI" : "URL";
                String identifier = isDoi
                    ? url.replaceAll("https?://(dx\\.)?doi.org/", "")
                    : url;

                relatedIdentifiers.add(new RelatedIdentifier(
                    identifier, idType, "IsReferencedBy", "", "", "", "Text"
                ));
            }
            return relatedIdentifiers;
        }

        public List<OnlineResource> filteredOnlineResources(List<OnlineResource> infoLinks) {
            return infoLinks.stream()
                .filter(infoLink -> infoLink.getUrl().startsWith("https://data-package.ceh.ac.uk/sd/"))
                .toList();
        }

        public List<Identifier> getAlternateResourceIdentifiers(List<ResourceIdentifier> resourceIdentifiers) {
            return resourceIdentifiers.stream()
                .filter(resourceIdentifier -> (!"doi:".equals(resourceIdentifier.getCodeSpace())))
                .filter(resourceIdentifier -> !url.equals(resourceIdentifier.getCoupledResource()))
                .map(resourceIdentifier -> new Identifier(
                    resourceIdentifier.getCoupledResource(),
                    resourceIdentifier.getCoupledResource().startsWith("http") ? "URL" : "URN"))
                .collect(Collectors.toList());
        }

        public List<Date> setDateDetails(DatasetReferenceDate datasetReferenceDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return Stream.of(
                    new Date(datasetReferenceDate.getPublicationDate().format(formatter), "Submitted", ""),
                    Optional.ofNullable(datasetReferenceDate.getCreationDate())
                        .map(date -> new Date(date.format(formatter), "Created", ""))
                        .orElse(null),
                    Optional.ofNullable(datasetReferenceDate.getReleasedDate())
                        .map(date -> new Date(date.format(formatter), "Available", ""))
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        public List<Subject> extractSubjects(List<Keyword> keywords) {
            List<Subject> subjects = new ArrayList<>();

            for (Keyword keyword : keywords) {
                String uri = keyword.getUri() != null ? keyword.getUri().trim() : "";
                String value = keyword.getValue() != null ? keyword.getValue().trim() : "";

                String subjectScheme = "";
                String schemeUri = "";

                if (!uri.isEmpty()) {
                    if (uri.matches("^https?://inspire\\.ec\\.europa\\.eu/\\S+$")) {
                        subjectScheme = "European Union INSPIRE registry";
                        schemeUri = "http://inspire.ec.europa.eu/registry/";
                    } else if (uri.matches("^https?://www\\.wikidata\\.org/entity/\\S+$")) {
                        subjectScheme = "Wikidata";
                        schemeUri = "https://www.wikidata.org/";
                    } else if (uri.matches("^https?://sws\\.geonames\\.org/\\S+$")) {
                        subjectScheme = "Geonames";
                        schemeUri = "http://www.geonames.org/";
                    } else if (uri.matches("^https?://www\\.eionet\\.europa\\.eu/gemet/concept/\\S+$")) {
                        subjectScheme = "GEMET concepts";
                        schemeUri = "https://www.eionet.europa.eu/gemet/";
                    }
                }
                if (!value.isEmpty()) {
                    subjects.add(new Subject(
                        value,
                        subjectScheme.isEmpty() ? null : subjectScheme,
                        schemeUri.isEmpty() ? null : schemeUri,
                        uri.isEmpty() ? null : uri,
                        null
                    ));
                }
            }

            return subjects;
        }

        public int publicationDateCheck(LocalDate publicationDate) {
            int year = 0;
            if (publicationDate != null) {
                year = publicationDate.getYear();
            }
            return year;
        }

        public Publisher assignPublisher(List<ResponsibleParty> publishers) {
            return publishers.stream()
                .findFirst()
                .map(assignedPublisher -> {
                    if (assignedPublisher.isRor()) {
                        return new Publisher(assignedPublisher.getOrganisationName(),
                            assignedPublisher.getOrganisationIdentifier(),
                            "ROR",
                            "https://ror.org/");
                    } else {
                        return new Publisher(assignedPublisher.getOrganisationName());
                    }
                })
                .orElse(null);
        }

        public List<DataciteContact> dataciteContact(GeminiDocument document, String contactType) {
            List<DataciteContact> contacts = new LinkedList<>();

            if (contactType.equals("creator")) {
                for (ResponsibleParty author : document.getAuthors()) {
                    contacts.add(dataciteContactHelper(author, "creator", null));
                }
            } else if (contactType.equals("contributor")) {
                if (!document.getPointsOfContact().isEmpty()
                    || !document.getRightsHolders().isEmpty()
                    || !document.getCustodians().isEmpty()) {

                    for (ResponsibleParty poc : document.getPointsOfContact()) {
                        contacts.add(dataciteContactHelper(poc, "contributor", "ContactPerson"));
                    }
                    for (ResponsibleParty rh : document.getRightsHolders()) {
                        contacts.add(dataciteContactHelper(rh, "contributor", "RightsHolder"));
                    }
                    for (ResponsibleParty custodian : document.getCustodians()) {
                        contacts.add(dataciteContactHelper(custodian, "contributor", "HostingInstitution"));
                    }
                }
            }
            return contacts;
        }

        public DataciteContact dataciteContactHelper(ResponsibleParty party, String contactType, String role) {
            DataciteContact details = new DataciteContact();
            Affiliation affiliation;
            if (contactType.equals("contributor") && role != null && !role.isEmpty()) {
                details.setContributorType(role);
            }

            boolean hasFullName = party.getFullName() != null && !party.getFullName().isEmpty();
            if (hasFullName) {
                details.setName(party.getFullName());
                details.setNameType("Personal");
                if (!party.getGivenName().isEmpty()) {
                    details.setGivenName(party.getGivenName());
                }
                if (!party.getFamilyName().isEmpty()) {
                    details.setFamilyName(party.getFamilyName());
                }
                NameIdentifier identifier = new NameIdentifier(party.getNameIdentifier(), "", "");
                if (party.isOrcid()) {
                    identifier.setNameIdentifierScheme("ORCID");
                    identifier.setSchemeUri("https://orcid.org/");
                    details.setNameIdentifiers(List.of(identifier));
                } else {
                    identifier.setNameIdentifierScheme("Other");
                    details.setNameIdentifiers(List.of(identifier));
                }

                if (party.isRor()) {
                    affiliation = new Affiliation(party.getOrganisationName(),party.getOrganisationIdentifier(),
                                            "ROR","https://ror.org");
                    details.setAffiliation(List.of(affiliation));
                } else if (!party.getOrganisationName().isEmpty()) {
                    affiliation = new Affiliation(party.getOrganisationName(),"","","");
                    details.setAffiliation(List.of(affiliation));
                }
            } else {
                details.setName(party.getOrganisationName());
                details.setNameType("Organizational");

                if (party.isRor()) {
                    affiliation = new Affiliation(party.getOrganisationName(), party.getOrganisationIdentifier(),
                                        "ROR", "");
                    details.setAffiliation(List.of(affiliation));
                }
            }
            return details;
        }
    }
}
