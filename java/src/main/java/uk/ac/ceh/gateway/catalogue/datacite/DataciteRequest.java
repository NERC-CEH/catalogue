package uk.ac.ceh.gateway.catalogue.datacite;

import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.*;

@Value
public class DataciteRequest {
    Data data;

    public DataciteRequest(Map<String, Object> request, String url) {//this will take a Map request which includes the doi string, so separate doi sting not needed?
        // url string and requestMap needed
        String doi = request.get("doi").toString();
        GeminiDocument document = (GeminiDocument) request.get("doc");
        String resourceType = request.get("resourceType").toString();
        this.data = new Data(doi, new Attributes(doi, document, url, resourceType));
    }

    @Value
    public static class Data {
        String id; // this is the DOI
        String type = "dois";
        Attributes attributes;
    }

    @Value
    public static class Attributes {

        public Attributes(String doi, GeminiDocument document, String url, String resourceType) {
            this.doi = doi;
//            this.xml = new String(Base64.encodeBase64(xml.getBytes()));
            this.url = url;
            this.document = document;
            this.titles = List.of(new Title(document.getTitle()));
            this.resourceType = resourceType;
            this.creators = dataciteContact(document, "creator");
            this.contributors = dataciteContact(document, "contributor");
            this.publisher = assignPublisher(document);
            this.publicationYear = publicationDateCheck(document);
            this.subjects = extractSubjects(document);
        }

        String doi;
        String event = "publish";
        String url; // url of DOI landing page
        GeminiDocument document;
        List<Title> titles;
        String resourceType;
        List<Map<String, Object>> creators;
        Publisher publisher;
        int publicationYear;
        List<Map<String, Object>> contributors;
        List<Subject> subjects;

        @Value
        public static class Title {
            String title;
            public Title(String title) {
                this.title = title;
            }
        }
        @Value
        public static class Publisher {
            String name;
            String publisherIdentifier;
            String publisherIdentifierScheme;
            String schemeUri;
            public Publisher(String name, String publisherIdentifier, String publisherIdentifierScheme, String schemeUri) {
                this.name = name;
                this.publisherIdentifier = publisherIdentifier;
                this.publisherIdentifierScheme = publisherIdentifierScheme;
                this.schemeUri = schemeUri;
            }
            public Publisher(String name) {
                this.name = name;
                this.publisherIdentifier = "";
                this.publisherIdentifierScheme = "";
                this.schemeUri = "";
            }
        }

        @Value
        public static class Subject {
            String subject;
            String subjectScheme;
            String schemeUri;
            String valueUri;
            String classificationCode;

            public Subject(String subject, String subjectScheme, String schemeUri, String valueUri, String classificationCode) {
                this.subject = subject;
                this.subjectScheme = subjectScheme;
                this.schemeUri = schemeUri;
                this.valueUri = valueUri;
                this.classificationCode = classificationCode;
            }
        }

        public List<Subject> extractSubjects(GeminiDocument document) {
            List<Subject> subjects = new ArrayList<>();

            for (Keyword keyword : document.getAllKeywords()) {
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


        public int publicationDateCheck(GeminiDocument document) {
            int year = 0;
            if (document.getDatasetReferenceDate().getPublicationDate() != null) {
                year = document.getDatasetReferenceDate().getPublicationDate().getYear();
            }
            return year;
        }
        public Publisher assignPublisher(GeminiDocument document) {
            Publisher publisher = null;
            List<ResponsibleParty> publishers = document.getPublishers();
            ResponsibleParty assignedPublisher = null;
            if (publishers != null && !publishers.isEmpty()) {
                assignedPublisher = document.getPublishers().getFirst();
            }
            if (assignedPublisher != null) {
                if (assignedPublisher.isRor()) {
                    publisher = new Publisher(assignedPublisher.getOrganisationName(),
                        assignedPublisher.getOrganisationIdentifier(),
                        "ROR",
                        "https://ror.org/");
                } else {
                    publisher = new Publisher(assignedPublisher.getOrganisationName());
                }
            }
            return publisher;
        }
        //purpose of ticket is to not use xml and the datacite, plus included, templates and instead have the geminidoc contents translated into Attributes
        //e.g. String title = document.getTitle();
        //will need to reverse engineer the template to get all required fields into the Attributes object
//        String xml; // base64 encoded Datacite xml

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
