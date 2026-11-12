package uk.ac.ceh.gateway.catalogue.people;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.RemoteSolrException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.SolrVisibilityFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

/**
 * Finds the people the catalogue already names with an ORCID, for the editor's people look-up
 * (dri-one #534). Only ORCIDs a record already holds are offered: ORCID asks that iDs not be matched
 * to people by searching its registry by name, so this never does.
 *
 * <p>It searches the records the editor may see, under search's own visibility filter, so a name
 * that is only in someone else's draft is never offered. At most {@value #ROWS} records are read for
 * a term, so for a very common name a person's record count is how many of those name them.
 */
@Slf4j
@Service
public class PeopleSearchService {
    static final String COLLECTION = "documents";
    static final int ROWS = 500;
    static final int LIMIT = 10;
    private static final int AFFILIATIONS = 3;

    private final SolrClient solrClient;
    private final GroupStore<CatalogueUser> groupStore;

    public PeopleSearchService(SolrClient solrClient, GroupStore<CatalogueUser> groupStore) {
        this.solrClient = solrClient;
        this.groupStore = groupStore;
    }

    /**
     * The people matching a term, most often named first. A term is a name, each word of which must
     * begin a word of the person's name ("c rowl" finds Clare Rowland), or an ORCID iD.
     */
    public List<KnownPerson> search(String term, CatalogueUser user) throws SolrServerException {
        var orcid = Orcid.id(term);
        var words = words(term);
        String q;
        if (orcid.isPresent()) {
            q = "orcid:\"" + orcid.get() + "\"";
        } else {
            // A one-letter word is an initial the index doesn't keep, so it only narrows the matches
            q = words.stream()
                .filter(word -> word.length() > 1)
                .map(word -> "personName:" + ClientUtils.escapeQueryChars(word) + "*")
                .collect(Collectors.joining(" AND "));
        }
        if (q.isEmpty()) return List.of();

        var query = new SolrQuery(q);
        query.setFields("person");
        query.setRows(ROWS);
        SolrVisibilityFilter.applyAcrossCatalogues(query, user, groupStore);

        try {
            var documents = solrClient.query(COLLECTION, query, POST).getResults();
            return people(documents, person -> orcid.map(id -> id.equals(person.orcid())).orElseGet(() -> matches(person, words)));
        } catch (IOException | SolrServerException | RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }

    static List<String> words(String term) {
        if (term == null) return List.of();
        return Arrays.stream(term.toLowerCase(Locale.ROOT).split("[^\\p{L}]+"))
            .filter(word -> !word.isEmpty())
            .distinct()
            .limit(6)
            .toList();
    }

    private static boolean matches(NamedPerson person, List<String> words) {
        var names = words(person.name());
        return words.stream().allMatch(word -> names.stream().anyMatch(name -> name.startsWith(word)));
    }

    /**
     * The people any of whose names match, with every name and organisation the records give them:
     * searching "clare" also finds Clare Rowland's records that call her "C. Rowland", and counts them.
     */
    private static List<KnownPerson> people(Collection<SolrDocument> documents, Function<NamedPerson, Boolean> wanted) {
        var records = documents.stream()
            .map(document -> Optional.ofNullable(document.getFieldValues("person")).orElse(List.of()).stream()
                .map(String::valueOf)
                .map(NamedPerson::unpack)
                .flatMap(Optional::stream)
                .toList())
            .toList();
        Set<String> matched = records.stream()
            .flatMap(List::stream)
            .filter(wanted::apply)
            .map(NamedPerson::orcid)
            .collect(Collectors.toSet());

        Map<String, Tally> tallies = new LinkedHashMap<>();
        for (var people : records) {
            Map<String, Set<NamedPerson>> inRecord = new LinkedHashMap<>();
            people.stream()
                .filter(person -> matched.contains(person.orcid()))
                .forEach(person -> inRecord.computeIfAbsent(person.orcid(), id -> new LinkedHashSet<>()).add(person));
            inRecord.forEach((id, named) -> tallies.computeIfAbsent(id, Tally::new).add(named));
        }
        return tallies.values().stream()
            .sorted(Comparator.comparingInt(Tally::records).reversed())
            .limit(LIMIT)
            .map(Tally::person)
            .toList();
    }

    /** One person's appearances in the records read: one record counts once however often it names them */
    private static final class Tally {
        private final String orcid;
        private int records;
        private final Map<List<String>, Integer> names = new HashMap<>();
        private final Map<KnownPerson.Affiliation, Integer> affiliations = new HashMap<>();

        Tally(String orcid) {
            this.orcid = orcid;
        }

        int records() {
            return records;
        }

        void add(Set<NamedPerson> named) {
            records++;
            named.stream().map(p -> List.of(p.givenName(), p.familyName())).distinct()
                .forEach(name -> names.merge(name, 1, Integer::sum));
            named.stream()
                .filter(p -> !p.organisationName().isBlank())
                .map(p -> new KnownPerson.Affiliation(p.organisationName(), p.organisationIdentifier()))
                .distinct()
                .forEach(affiliation -> affiliations.merge(affiliation, 1, Integer::sum));
        }

        KnownPerson person() {
            var byUse = mostUsedFirst(names);
            var name = byUse.getFirst();
            return new KnownPerson(
                Orcid.uri(orcid),
                name.get(0),
                name.get(1),
                otherNames(byUse),
                mostUsedFirst(affiliations).stream().limit(AFFILIATIONS).toList(),
                records
            );
        }

        /**
         * The names after the most used, once each: "M. Botham," is "M. Botham" with a stray comma,
         * not another name, so names differing only in case or punctuation count as one
         */
        private static List<String> otherNames(List<List<String>> byUse) {
            Set<List<String>> seen = new HashSet<>();
            seen.add(words(String.join(" ", byUse.getFirst())));
            return byUse.stream().skip(1)
                .map(n -> String.join(" ", n).strip())
                .filter(n -> seen.add(words(n)))
                .toList();
        }

        private static <T> List<T> mostUsedFirst(Map<T, Integer> counts) {
            return counts.entrySet().stream()
                .sorted(Map.Entry.<T, Integer>comparingByValue().reversed().thenComparing(e -> String.valueOf(e.getKey())))
                .map(Map.Entry::getKey)
                .toList();
        }
    }
}
