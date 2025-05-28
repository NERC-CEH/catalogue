package uk.ac.ceh.gateway.catalogue.datacite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class DataciteRequest {
    Data data;

    public DataciteRequest(Map<String, Object> request, String url, JenaLookupService jenaLookupService) {
        String doi = request.get("doi").toString();
        GeminiDocument document = (GeminiDocument) request.get("doc");
        String resourceType = Optional.ofNullable(request.get("resourceType"))
            .map(Object::toString)
            .orElse(null);
        this.data = new Data(doi, new Attributes(doi, document, url, resourceType, jenaLookupService));
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
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

        public record DataciteContact(String contributorType, String name, String nameType, String givenName, String familyName, List<NameIdentifier> nameIdentifiers, List<Affiliation> affiliation) {}
        public record NameIdentifier(String nameIdentifier, String nameIdentifierScheme, String schemeUri) {}
        public record Affiliation(String name, String affiliationIdentifier, String affiliationIdentifierScheme, String schemeUri) {}
        public record Title(String title) {}
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
                        return new Publisher(assignedPublisher.getOrganisationName(),
                        null,
                        null,
                        null);
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
            String contributorType = (contactType.equals("contributor") && role != null && !role.isEmpty()) ? role : null;

            String name;
            String nameType;
            String givenName = null;
            String familyName = null;
            List<NameIdentifier> nameIdentifiers = null;
            List<Affiliation> affiliation = null;

            boolean hasFullName = party.getFullName() != null && !party.getFullName().isEmpty();

            if (hasFullName) {
                name = party.getFullName();
                nameType = "Personal";

                if (!party.getGivenName().isEmpty()) {
                    givenName = party.getGivenName();
                }

                if (!party.getFamilyName().isEmpty()) {
                    familyName = party.getFamilyName();
                }

                NameIdentifier identifier = new NameIdentifier(
                    party.getNameIdentifier(),
                    party.isOrcid() ? "ORCID" : "Other",
                    party.isOrcid() ? "https://orcid.org/" : ""
                );
                nameIdentifiers = List.of(identifier);

                if (party.isRor()) {
                    affiliation = List.of(new Affiliation(
                        party.getOrganisationName(),
                        party.getOrganisationIdentifier(),
                        "ROR",
                        "https://ror.org"
                    ));
                } else if (!party.getOrganisationName().isEmpty()) {
                    affiliation = List.of(new Affiliation(
                        party.getOrganisationName(), "", "", ""
                    ));
                }
            } else {
                name = party.getOrganisationName();
                nameType = "Organizational";

                if (party.isRor()) {
                    affiliation = List.of(new Affiliation(
                        party.getOrganisationName(),
                        party.getOrganisationIdentifier(),
                        "ROR",
                        ""
                    ));
                }
            }

            return new DataciteContact(
                contributorType,
                name,
                nameType,
                givenName,
                familyName,
                nameIdentifiers,
                affiliation
            );
        }

    }
}
