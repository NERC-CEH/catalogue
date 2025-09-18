package uk.ac.ceh.gateway.catalogue.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class CffHarvestService {
    private final RestTemplate restTemplate;

    public CffHarvestService(@Qualifier("normal") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public GeminiDocument createGeminiFromCff(String githubUrl) {
        String rawUrl = toRawGithubUrl(githubUrl);
        log.info("Fetching CFF from {}", rawUrl);

        String yamlText = restTemplate.getForObject(rawUrl, String.class);
        if (yamlText == null) {
            throw new RuntimeException("Empty response from GitHub: " + rawUrl);
        }

        Map<String, Object> cff = (Map<String, Object>) new Yaml().load(yamlText);
        log.info("Parsed CFF fields: {}", cff.keySet());

        GeminiDocument doc = new GeminiDocument();
        doc.setTitle((String) cff.get("title"));
        doc.setDescription((String) cff.get("abstract"));

        setResourceType(doc);
        setDates(doc, (String) cff.get("date-published"), (String) cff.get("date-released"));
        setResponsibleParties(doc, cff);
        setIdentifiers(doc, (String) cff.get("doi"));
        setLicense(doc, (String) cff.get("license"));
        setKeywords(doc, cff.get("keywords"));
        setOnlineResources(doc, (String) cff.get("repository"), (String) cff.get("repository-code"), (String) cff.get("url"));

        return doc;
    }

    private String toRawGithubUrl(String githubUrl) {
        return githubUrl
            .replace("github.com/", "raw.githubusercontent.com/")
            .replace("/blob/", "/");
    }

    private void setResourceType(GeminiDocument doc) {
        doc.setResourceType(
            Keyword.builder()
                .value("application")
                .build()
        );
    }

    private void setDates(GeminiDocument doc, String pubDateStr, String relDateStr) {
        try {
            LocalDate pubDate = pubDateStr != null ? LocalDate.parse(pubDateStr) : null;
            LocalDate relDate = relDateStr != null ? LocalDate.parse(relDateStr) : null;

            if (pubDate != null || relDate != null) {
                doc.setDatasetReferenceDate(
                    DatasetReferenceDate.builder()
                        .publicationDate(pubDate)
                        .releasedDate(relDate)
                        .build()
                );
            }
        } catch (Exception e) {
            log.warn("Could not parse dates: {}", e.getMessage());
        }
    }

    private void setResponsibleParties(GeminiDocument doc, Map<String, Object> cff) {
        List<ResponsibleParty> parties = new ArrayList<>();

        Object authorsObj = cff.get("authors");
        if (authorsObj instanceof List<?> authors) {
            for (Object a : authors) {
                if (a instanceof Map<?, ?> author) {
                    parties.add(ResponsibleParty.builder()
                        .role("author")
                        .familyName((String) author.get("family-names"))
                        .givenName((String) author.get("given-names"))
                        .organisationName((String) author.get("affiliation"))
                        .email((String) author.get("email"))
                        .nameIdentifier((String) author.get("orcid"))
                        .displayName(buildDisplayName(
                            (String) author.get("given-names"),
                            (String) author.get("family-names")))
                        .build());
                }
            }
        }

        Object contactsObj = cff.get("contact");
        if (contactsObj instanceof Iterable<?> contacts) {
            for (Object c : contacts) {
                if (c instanceof Map<?, ?> contact) {
                    parties.add(ResponsibleParty.builder()
                        .role("pointOfContact")
                        .givenName((String) contact.get("given-names"))
                        .familyName((String) contact.get("family-names"))
                        .organisationName((String) contact.get("affiliation"))
                        .email((String) contact.get("email"))
                        .nameIdentifier((String) contact.get("orcid"))
                        .build());
                }
            }
        }

        if (!parties.isEmpty()) {
            doc.setResponsibleParties(parties);
        }
    }

    private void setIdentifiers(GeminiDocument doc, String doi) {
        if (doi != null && !doi.isBlank()) {
            doc.setResourceIdentifiers(
                List.of(ResourceIdentifier.builder()
                    .code(doi)
                    .codeSpace("doi")
                    .version("")
                    .build())
            );
        }
    }

    private void setLicense(GeminiDocument doc, String license) {
        if (license != null && !license.isBlank()) {
            doc.setUseConstraints(
                List.of(ResourceConstraint.builder()
                    .code("license")
                    .value(license)
                    .uri("")
                    .build())
            );
        }
    }

    private void setKeywords(GeminiDocument doc, Object kwObj) {
        if (kwObj instanceof Iterable<?> kws) {
            List<Keyword> keywords = new ArrayList<>();
            for (Object k : kws) {
                if (k == null) continue;
                String kw = k.toString().trim();
                if (kw.startsWith("http://") || kw.startsWith("https://")) {
                    String[] parts = kw.split("/");
                    String last = parts.length > 0 ? parts[parts.length - 1] : kw;
                    keywords.add(Keyword.builder().value(last).URI(kw).build());
                } else {
                    keywords.add(Keyword.builder().value(kw).build());
                }
            }
            if (!keywords.isEmpty()) {
                doc.setKeywordsOther(keywords);
            }
        }
    }

    private void setOnlineResources(GeminiDocument doc, String repo, String repoCode, String url) {
        List<OnlineResource> resources = new ArrayList<>();

        if (repo != null && !repo.isBlank()) {
            resources.add(OnlineResource.builder()
                .url(repo).name("Repository").function("information").build());
        }
        if (repoCode != null && !repoCode.isBlank()) {
            resources.add(OnlineResource.builder()
                .url(repoCode).name("Repository Code").function("information").build());
        }
        if (url != null && !url.isBlank()) {
            resources.add(OnlineResource.builder()
                .url(url).name("Project URL").function("information").build());
        }

        if (!resources.isEmpty()) {
            doc.setOnlineResources(resources);
        }
    }

    private String buildDisplayName(String given, String family) {
        if (given == null && family == null) return "";
        return (given == null ? "" : given) + " " + (family == null ? "" : family);
    }
}
