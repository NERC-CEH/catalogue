package uk.ac.ceh.gateway.catalogue.config;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.search.HardcodedFacetFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;

class CatalogueServiceConfigTest {

    private final CatalogueService catalogues = new CatalogueServiceConfig().eidcCatalogue();

    @Test
    void allCataloguesExposesTheFdriFacets() {
        //when
        val facetKeys = catalogues.retrieve(CatalogueService.ALL_CATALOGUES_ID).getFacetKeys();

        //then
        assertThat(facetKeys, hasItems(
            "fdriCatchment",
            "fdriCategory",
            "fdriSpatialScale",
            "fdriTimeseriesData"
        ));
    }

    /*
     * HardcodedFacetFactory.newInstances discards keys it does not recognise, so
     * a facet key with no matching case is dropped without any error - the facet
     * simply never appears in the search UI.
     */
    @Test
    void everyFacetKeyResolvesToAFacet() {
        //given
        val factory = new HardcodedFacetFactory();

        //when
        val unresolved = catalogues.retrieveAll()
            .stream()
            .flatMap(catalogue -> catalogue.getFacetKeys().stream())
            .distinct()
            .filter(key -> factory.newInstance(key) == null)
            .sorted()
            .toList();

        //then
        assertThat("Facet keys with no HardcodedFacetFactory case", unresolved, empty());
    }

    @Test
    void fdriFacetKeysMapToMatchingSolrFieldNames() {
        //given
        val factory = new HardcodedFacetFactory();

        //when
        val fieldNames = factory.newInstances(java.util.List.of(
            "fdriCatchment", "fdriCategory", "fdriSpatialScale", "fdriTimeseriesData"
        )).stream().map(facet -> facet.getFieldName()).toList();

        //then
        assertThat(fieldNames, contains(
            "fdriCatchment", "fdriCategory", "fdriSpatialScale", "fdriTimeseriesData"
        ));
    }
}
