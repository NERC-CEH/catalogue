package uk.ac.ceh.gateway.catalogue.datacite;

import lombok.AllArgsConstructor;
import lombok.Value;
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

@Value
public class DataciteRequest {
    Data data;

    public DataciteRequest(Map<String, Object> request, String url, JenaLookupService jenaLookupService) {//this will take a Map request which includes the doi string, so separate doi sting not needed?
        // url string and requestMap needed
        String doi = request.get("doi").toString();
        GeminiDocument document = (GeminiDocument) request.get("doc");
        String resourceType = Optional.ofNullable(request.get("resourceType"))
            .map(Object::toString)
            .orElse(null);
        this.data = new Data(doi, new Attributes(doi, document, url, resourceType, jenaLookupService));
    }

    @Value
    public static class Data {
        String id; // this is the DOI
        String type = "dois";
        Attributes attributes;
    }

    @Value
    public static class Attributes {

        public Attributes(String doi, GeminiDocument document, String url, String resourceType, JenaLookupService jenaLookupService) {
            this.doi = doi;
//            this.xml = new String(Base64.encodeBase64(xml.getBytes()));
            this.url = url;
            this.titles = List.of(new Title(document.getTitle()));
            this.resourceType = resourceType;
            this.creators = dataciteContact(document, "creator");
            this.contributors = dataciteContact(document, "contributor");
            this.publisher = assignPublisher(document.getPublishers());
            this.publicationYear = publicationDateCheck(document.getDatasetReferenceDate().getPublicationDate());
            this.subjects = extractSubjects(document.getAllKeywords());
            this.dates = setDateDetails(document.getDatasetReferenceDate());
            this.language = "en";
            this.alternateIdentifiers = getAlternateResourceIdentifiers(document.getResourceIdentifiers());
            this.relatedIdentifiers = createRelatedIdentifiers(document, jenaLookupService);
            this.formats = gatherDistributionFormats(document.getDistributionFormats());
            this.rightsList = listRights(document.getLicences());
            this.descriptions = populateDescriptions(document);
            this.geoLocations = extractGeoLocations(document.getBoundingBoxes());
            this.fundingReferences = fundingDetails(document.getFunding());
        }

        String doi;
        String event = "publish";
        String url; // url of DOI landing page
        List<Title> titles;
        String resourceType;
        List<Map<String, Object>> creators;
        Publisher publisher;
        int publicationYear;
        List<Map<String, Object>> contributors;
        List<Subject> subjects;
        List<Date> dates;
        String language;
        List<AlternateIdentifier> alternateIdentifiers;
        List<RelatedIdentifier> relatedIdentifiers;
        List<String> formats;
        List<Rights> rightsList;
        List<Description> descriptions;
        List<GeoLocation> geoLocations;
        List<FundingReference> fundingReferences;

        @Value
        @AllArgsConstructor
        public static class Title {
            String title;
        }
        @Value
        @AllArgsConstructor
        public static class Publisher {
            String name;
            String publisherIdentifier;
            String publisherIdentifierScheme;
            String schemeUri;

            public Publisher(String name) {
                this.name = name;
                this.publisherIdentifier = "";
                this.publisherIdentifierScheme = "";
                this.schemeUri = "";
            }
        }

        @Value
        @AllArgsConstructor
        public static class Subject {
            String subject;
            String subjectScheme;
            String schemeUri;
            String valueUri;
            String classificationCode;
        }

        @Value
        @AllArgsConstructor
        public static class Date {
            String date;
            String dateType;
            String dateInformation;
        }

        @Value
        @AllArgsConstructor
        public static class AlternateIdentifier {
            String alternateIdentifier;
            String alternateIdentifierType;
        }

        @Value
        @AllArgsConstructor
        public static class RelatedIdentifier {
            String relatedIdentifier;
            String relatedIdentifierType;
            String relationType;
            String relatedMetadataScheme;
            String schemeUri;
            String schemeType;
            String resourceTypeGeneral;
        }

        @Value
        @AllArgsConstructor
        public static class Rights {
            String rights;
            String lang;
            String rightsUri;
            String rightsIdentifier;
            String rightsIdentifierScheme;
            String schemeUri;
        }

        @Value
        @AllArgsConstructor
        public static class Description {
            String description;
            String lang;
            String descriptionType;
        }

        @Value
        @AllArgsConstructor
        public static class GeoLocation {
            GeoLocationBox geoLocationBox;
        }

        @Value
        @AllArgsConstructor
        public static class GeoLocationBox {
            BigDecimal westBoundLongitude;
            BigDecimal eastBoundLongitude;
            BigDecimal southBoundLatitude;
            BigDecimal northBoundLatitude;
        }

        @Value
        @AllArgsConstructor
        public static class FundingReference {
            String funderName;
            String funderIdentifier;
            String funderIdentifierType;
            String schemeUri;
            String awardNumber;
            String awardUri;
            String awardTitle;
        }
        //purpose of ticket is to not use xml and the datacite, plus included, templates and instead have the geminidoc contents translated into Attributes
        //e.g. String title = document.getTitle();
        //will need to reverse engineer the template to get all required fields into the Attributes object
//        String xml; // base64 encoded Datacite xml
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
                    document.getLineage().isEmpty() ? null : new Description(document.getLineage(), "", "Methods")
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

            List<OnlineResource> infoLinks = document.getInfoLinks();
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
            for (Supplemental supplemental : document.getIncomingCitations()) {
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

        public List<AlternateIdentifier> getAlternateResourceIdentifiers(List<ResourceIdentifier> resourceIdentifiers) {
            return resourceIdentifiers.stream()
                .filter(resourceIdentifier -> !"doi:".equals(resourceIdentifier.getCodeSpace()))
                .map(resourceIdentifier -> new AlternateIdentifier(
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


        public List<Map<String, Object>> dataciteContact(GeminiDocument document, String contactType) {
            List<Map<String, Object>> contacts = new LinkedList<>();

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


        public Map<String, Object> dataciteContactHelper(ResponsibleParty party, String contactType, String role) {
            Map<String, Object> details = new HashMap<>();

            if (contactType.equals("contributor") && role != null && !role.isEmpty()) {
                details.put("contributorType", role);
            }

            boolean hasFullName = party.getFullName() != null && !party.getFullName().isEmpty();
            if (hasFullName) {
                details.put("name", party.getFullName());
                details.put("nameType", "Personal");
                if (!party.getGivenName().isEmpty()) {
                    details.put("givenName", party.getGivenName());
                }
                if (!party.getFamilyName().isEmpty()) {
                    details.put("familyName", party.getFamilyName());
                }
                if (party.isOrcid()) {
                    Map<String, Object> identifier = Map.of(
                        "nameIdentifier", party.getNameIdentifier(),
                        "nameIdentifierScheme", "ORCID",
                        "schemeUri", "https://orcid.org/"
                    );
                    details.put("nameIdentifiers", List.of(identifier));
                }
                if (party.isRor()) {
                    Map<String, Object> affiliation = Map.of(
                        "name", party.getOrganisationName(),
                        "affiliationIdentifier", party.getOrganisationIdentifier(),
                        "affiliationIdentifierScheme", "ROR"
                    );
                    details.put("affiliation", List.of(affiliation));
                } else if (!party.getOrganisationName().isEmpty()) {
                    Map<String, Object> affiliation = Map.of("name", party.getOrganisationName());
                    details.put("affiliation", List.of(affiliation));
                }
            } else {
                details.put("name", party.getOrganisationName());
                details.put("nameType", "Organizational");
                if (party.isRor()) {
                    Map<String, Object> identifier = Map.of(
                        "nameIdentifier", party.getOrganisationIdentifier(),
                        "nameIdentifierScheme", "ROR"
                    );
                    details.put("nameIdentifiers", List.of(identifier));
                }
            }
            return details;
        }
    }
}
