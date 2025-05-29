package uk.ac.ceh.gateway.catalogue.datacite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DataciteRequestService {
    public List<DataciteRequest.Attributes.FundingReference> fundingDetails(List<Funding> funders) {
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

                return new DataciteRequest.Attributes.FundingReference(
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

    public List<DataciteRequest.Attributes.GeoLocation> extractGeoLocations(List<BoundingBox> boundingBoxes) {
        List<DataciteRequest.Attributes.GeoLocation> geoLocations = new ArrayList<>();

        for (BoundingBox boundingBox : boundingBoxes) {
            DataciteRequest.Attributes.GeoLocationBox box = new DataciteRequest.Attributes.GeoLocationBox(
                boundingBox.getWestBoundLongitude(),
                boundingBox.getEastBoundLongitude(),
                boundingBox.getSouthBoundLatitude(),
                boundingBox.getNorthBoundLatitude()
            );
            geoLocations.add(new DataciteRequest.Attributes.GeoLocation(box));
        }
        return geoLocations;
    }

    public List<DataciteRequest.Attributes.Description> populateDescriptions(GeminiDocument document) {
        return Stream.of(
                new DataciteRequest.Attributes.Description(document.getDescription(), "", "Abstract"),
                (document.getLineage() != null && !document.getLineage().isEmpty())
                    ? new DataciteRequest.Attributes.Description(document.getLineage(), "", "Methods")
                    : null
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<DataciteRequest.Attributes.Rights> listRights(List<ResourceConstraint> licences) {
        return licences.stream()
            .map(resourceConstraint -> {
                String value = resourceConstraint.getValue();
                String uri = resourceConstraint.getUri();
                boolean isLicense = "license".equals(resourceConstraint.getCode()) && !uri.isEmpty();

                if (isLicense) {
                    if ("https://eidc.ceh.ac.uk/licences/OGL/plain".equals(uri)) {
                        return new DataciteRequest.Attributes.Rights(value, "", uri, "OGL-UK-3.0", "SPDX", "");
                    }
                    return new DataciteRequest.Attributes.Rights(value, "", uri, "", "", "");
                } else {
                    return new DataciteRequest.Attributes.Rights(value, "", "", "", "", "");
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

    public List<DataciteRequest.Attributes.RelatedIdentifier> createRelatedIdentifiers(GeminiDocument document, JenaLookupService jenaLookupService) {
        List<DataciteRequest.Attributes.RelatedIdentifier> relatedIdentifiers = new ArrayList<>();

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
            relatedIdentifiers.add(new DataciteRequest.Attributes.RelatedIdentifier(
                resource.getUrl(), "URL", "IsDescribedBy", "", "", "", "Text"
            ));
        }
        for (Link link : relSupersedes) {
            relatedIdentifiers.add(new DataciteRequest.Attributes.RelatedIdentifier(
                link.getHref().replace("https://catalogue.ceh.ac.uk/id/", "10.5285/"),
                "DOI", "IsNewVersionOf", "", "", "", "Dataset"
            ));
        }
        for (Link link : relSupersedesBy) {
            relatedIdentifiers.add(new DataciteRequest.Attributes.RelatedIdentifier(
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

            relatedIdentifiers.add(new DataciteRequest.Attributes.RelatedIdentifier(
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

    public List<DataciteRequest.Attributes.Identifier> getAlternateResourceIdentifiers(List<ResourceIdentifier> resourceIdentifiers, String url) {
        return resourceIdentifiers.stream()
            .filter(resourceIdentifier -> (!"doi:".equals(resourceIdentifier.getCodeSpace())))
            .filter(resourceIdentifier -> !url.equals(resourceIdentifier.getCoupledResource()))
            .map(resourceIdentifier -> new DataciteRequest.Attributes.Identifier(
                resourceIdentifier.getCoupledResource(),
                resourceIdentifier.getCoupledResource().startsWith("http") ? "URL" : "URN"))
            .collect(Collectors.toList());
    }

    public List<DataciteRequest.Attributes.Date> setDateDetails(DatasetReferenceDate datasetReferenceDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return Stream.of(
                new DataciteRequest.Attributes.Date(datasetReferenceDate.getPublicationDate().format(formatter), "Submitted", ""),
                Optional.ofNullable(datasetReferenceDate.getCreationDate())
                    .map(date -> new DataciteRequest.Attributes.Date(date.format(formatter), "Created", ""))
                    .orElse(null),
                Optional.ofNullable(datasetReferenceDate.getReleasedDate())
                    .map(date -> new DataciteRequest.Attributes.Date(date.format(formatter), "Available", ""))
                    .orElse(null)
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<DataciteRequest.Attributes.Subject> extractSubjects(List<Keyword> keywords) {
        List<DataciteRequest.Attributes.Subject> subjects = new ArrayList<>();

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
                subjects.add(new DataciteRequest.Attributes.Subject(
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

    public DataciteRequest.Attributes.Publisher assignPublisher(List<ResponsibleParty> publishers) {
        return publishers.stream()
            .findFirst()
            .map(assignedPublisher -> {
                if (assignedPublisher.isRor()) {
                    return new DataciteRequest.Attributes.Publisher(assignedPublisher.getOrganisationName(),
                        assignedPublisher.getOrganisationIdentifier(),
                        "ROR",
                        "https://ror.org/");
                } else {
                    return new DataciteRequest.Attributes.Publisher(assignedPublisher.getOrganisationName(),
                        null,
                        null,
                        null);
                }
            })
            .orElse(null);
    }

    public List<DataciteRequest.Attributes.DataciteContact> dataciteContact(GeminiDocument document, String contactType) {
        List<DataciteRequest.Attributes.DataciteContact> contacts = new LinkedList<>();

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

    public DataciteRequest.Attributes.DataciteContact dataciteContactHelper(ResponsibleParty party, String contactType, String role) {
        String contributorType = (contactType.equals("contributor") && role != null && !role.isEmpty()) ? role : null;

        String name;
        String nameType;
        String givenName = null;
        String familyName = null;
        List<DataciteRequest.Attributes.NameIdentifier> nameIdentifiers = null;
        List<DataciteRequest.Attributes.Affiliation> affiliation = null;

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

            DataciteRequest.Attributes.NameIdentifier identifier = new DataciteRequest.Attributes.NameIdentifier(
                party.getNameIdentifier(),
                party.isOrcid() ? "ORCID" : "Other",
                party.isOrcid() ? "https://orcid.org/" : ""
            );
            nameIdentifiers = List.of(identifier);

            if (party.isRor()) {
                affiliation = List.of(new DataciteRequest.Attributes.Affiliation(
                    party.getOrganisationName(),
                    party.getOrganisationIdentifier(),
                    "ROR",
                    "https://ror.org"
                ));
            } else if (!party.getOrganisationName().isEmpty()) {
                affiliation = List.of(new DataciteRequest.Attributes.Affiliation(
                    party.getOrganisationName(), "", "", ""
                ));
            }
        } else {
            name = party.getOrganisationName();
            nameType = "Organizational";

            if (party.isRor()) {
                affiliation = List.of(new DataciteRequest.Attributes.Affiliation(
                    party.getOrganisationName(),
                    party.getOrganisationIdentifier(),
                    "ROR",
                    ""
                ));
            }
        }

        return new DataciteRequest.Attributes.DataciteContact(
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
