package uk.ac.ceh.gateway.catalogue.people;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Arrays;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("PeopleSearchService")
class PeopleSearchServiceTest {

    private static final NamedPerson CLARE = new NamedPerson("0000-0002-0459-506X", "Clare", "Rowland", "UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55");
    private static final NamedPerson CLARE_AS_C = new NamedPerson("0000-0002-0459-506X", "C.", "Rowland", "Lancaster University", "https://ror.org/04f2nsd36");
    private static final NamedPerson NAMESAKE = new NamedPerson("0000-0002-5474-5257", "Clare", "Rowland", "", "");
    private static final NamedPerson CLARE_WITH_COMMA = new NamedPerson("0000-0002-0459-506X", "Clare", "Rowland,", "UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55");
    private static final NamedPerson JOSIAH = new NamedPerson("0000-0002-1825-0097", "Josiah", "Carberry", "Brown University", "");

    @Mock private SolrClient solrClient;
    @Mock private GroupStore<CatalogueUser> groupStore;
    private PeopleSearchService service;
    private final CatalogueUser editor = new CatalogueUser("editor", "editor@example.com");

    @BeforeEach
    void setUp() {
        service = new PeopleSearchService(solrClient, groupStore);
    }

    private static SolrDocument record(NamedPerson... people) {
        var document = new SolrDocument();
        Arrays.stream(people).forEach(person -> document.addField("person", person.pack()));
        return document;
    }

    @SneakyThrows
    private ArgumentCaptor<SolrQuery> answer(SolrDocument... records) {
        var results = new SolrDocumentList();
        results.addAll(List.of(records));
        var response = mock(QueryResponse.class);
        given(response.getResults()).willReturn(results);
        var query = ArgumentCaptor.forClass(SolrQuery.class);
        given(solrClient.query(eq("documents"), query.capture(), eq(POST))).willReturn(response);
        return query;
    }

    @Test
    @SneakyThrows
    @DisplayName("offers each person once, most often named first, with how else they are named (a stray comma isn't another name) and where")
    void talliesPeople() {
        given(groupStore.getGroups(editor)).willReturn(List.of());
        answer(
            record(CLARE, JOSIAH),
            record(CLARE, CLARE_AS_C),
            record(NAMESAKE),
            record(CLARE),
            record(CLARE_WITH_COMMA)
        );

        var people = service.search("clare row", editor);

        assertThat(people, contains(
            new KnownPerson(
                "https://orcid.org/0000-0002-0459-506X", "Clare", "Rowland",
                List.of("C. Rowland"),
                List.of(
                    new KnownPerson.Affiliation("UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55"),
                    new KnownPerson.Affiliation("Lancaster University", "https://ror.org/04f2nsd36")
                ),
                4
            ),
            new KnownPerson("https://orcid.org/0000-0002-5474-5257", "Clare", "Rowland", List.of(), List.of(), 1)
        ));
    }

    @Test
    @SneakyThrows
    @DisplayName("searches the name's words as prefixes, under search's visibility filter for the editor")
    void queriesVisibleRecords() {
        var group = mock(Group.class);
        given(group.getName()).willReturn("CIG_EDITOR");
        given(groupStore.getGroups(editor)).willReturn(List.of(group));
        var query = answer();

        service.search("Clare Rowl", editor);

        assertThat(query.getValue().getQuery(), equalTo("personName:clare* AND personName:rowl*"));
        assertThat(query.getValue().getFields(), equalTo("person"));
        assertThat(query.getValue().getFilterQueries(), arrayContaining("view:(public OR editor OR cig_editor)"));
    }

    @Test
    @SneakyThrows
    @DisplayName("reads a publisher every record in the catalogues they publish, as their catalogue's search does")
    void publisherSeesTheirCatalogues() {
        var eidc = mock(Group.class);
        given(eidc.getName()).willReturn("ROLE_EIDC_PUBLISHER");
        var project = mock(Group.class);
        given(project.getName()).willReturn("PHTR");
        given(groupStore.getGroups(editor)).willReturn(List.of(eidc, project));
        var query = answer();

        service.search("botham", editor);

        assertThat(query.getValue().getFilterQueries(), arrayContaining(
            "view:(public OR editor OR role_eidc_publisher OR phtr) OR catalogue:eidc OR catalogue_view:eidc"));
    }

    @Test
    @SneakyThrows
    @DisplayName("shows someone not signed in only published, public records")
    void publicUserSeesPublishedOnly() {
        var query = answer();

        service.search("rowland", CatalogueUser.PUBLIC_USER);

        assertThat(query.getValue().getFilterQueries(), hasItemInArray("{!term f=state}published"));
        assertThat(query.getValue().getFilterQueries(), hasItemInArray("{!term f=view}public"));
    }

    @Test
    @SneakyThrows
    @DisplayName("narrows by an initial the index doesn't keep")
    void initialNarrows() {
        given(groupStore.getGroups(editor)).willReturn(List.of());
        var query = answer(record(CLARE, JOSIAH));

        var people = service.search("j carb", editor);

        assertThat(query.getValue().getQuery(), equalTo("personName:carb*"));
        assertThat(people.stream().map(KnownPerson::orcid).toList(), contains("https://orcid.org/0000-0002-1825-0097"));
    }

    @Test
    @SneakyThrows
    @DisplayName("finds a person by their iD, however it is typed")
    void byOrcid() {
        given(groupStore.getGroups(editor)).willReturn(List.of());
        var query = answer(record(CLARE, JOSIAH));

        var people = service.search("orcid.org/0000-0002-0459-506x", editor);

        assertThat(query.getValue().getQuery(), equalTo("orcid:\"0000-0002-0459-506X\""));
        assertThat(people.stream().map(KnownPerson::orcid).toList(), contains("https://orcid.org/0000-0002-0459-506X"));
    }

    @Test
    @SneakyThrows
    @DisplayName("asks nothing for a term with no name in it")
    void blankTerm() {
        assertThat(service.search(" - ", editor), empty());
        assertThat(service.search("c", editor), empty());
        verifyNoInteractions(solrClient);
    }

    @Test
    @DisplayName("splits a term into lower-case words of letters")
    void words() {
        assertThat(PeopleSearchService.words("O'Brien,  Seán"), contains("o", "brien", "seán"));
    }
}
