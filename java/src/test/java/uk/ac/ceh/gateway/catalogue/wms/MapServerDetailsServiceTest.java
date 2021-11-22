package uk.ac.ceh.gateway.catalogue.wms;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.DataSource.Attribute.Bucket;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition.Projection;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService.MapBucketDetails;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapServerDetailsServiceTest {
    private MapServerDetailsService service;

    @BeforeEach
    public void init() {
        service = new MapServerDetailsService("https://catalogue.ceh.ac.uk");
    }

    @Test
    public void checkThatNewDataDefinitionHasFalseByteType() {
        //Given
        DataSource ds = new DataSource();

        //When
        Boolean isDataBtye = ds.getBytetype();

        //Then
        assertThat(isDataBtye, is(false));

    }

    @Test
    void checkThatServiceGeminiDocumentWithServiceDefinitionIsHostable() {
        //Given
        val mapDataDefinition = new MapDataDefinition();
        mapDataDefinition.setData(List.of(new DataSource()));
        val document = new GeminiDocument();
        document.setType("service");
        document.setMapDataDefinition(mapDataDefinition);

        //When
        boolean isHostable = service.isMapServiceHostable(document);

        //Then
        assertTrue(isHostable);
    }

    @Test
    void checkThatServiceGeminiDocumentEmptyServiceDefinitionIsNotHostable() {
        //Given
        val mapDataDefinition = new MapDataDefinition();
        mapDataDefinition.setData(Collections.emptyList());
        val document = new GeminiDocument();
        document.setType("service");
        document.setMapDataDefinition(mapDataDefinition);

        //When
        boolean isHostable = service.isMapServiceHostable(document);

        //Then
        assertFalse(isHostable);
    }

    @Test
    void checkThatDatasetGeminiDocumentIsNotHostable() {
        //Given
        val document = new GeminiDocument();
        document.setType("dataset");

        //When
        boolean isHostable = service.isMapServiceHostable(document);

        //Then
        assertFalse(isHostable);
    }

    @Test
    public void checkThatRewritesToHostedMapserverRequest() {
        //Given
        String localRequest = "https://catalogue.ceh.ac.uk/maps/ID?REQUEST=WMS";

        //When
        String request = service.rewriteToLocalWmsRequest(localRequest);

        //Then
        assertEquals(request, "http://mapserver/ID?REQUEST=WMS");
    }

    @Test
    public void checkThatOtherServicePassesThrough() {
        //Given
        String externalRequest = "http://somewhere.out.side/as/A/wms?REQUEST=WMS";

        //When
        String request = service.rewriteToLocalWmsRequest(externalRequest);

        //Then
        assertEquals(request, externalRequest);
    }

    @Test
    public void checkThatCanBuildAMapServerRequestUrl() {
        //Given
        String file = "myfileid";
        String queryString = "query";

        //When
        String request = service.getLocalWMSRequest(file, queryString);

        //Then
        assertEquals(request, "http://mapserver/myfileid?query");
    }

    @Test
    public void checkThatCanListProjectionSystemsFromMapDataDefinition() {
        //Given
        MapDataDefinition map = mock(MapDataDefinition.class);
        DataSource datasource = mock(DataSource.class);
        when(datasource.getEpsgCode()).thenReturn("CODE1");
        Projection reproj = mock(Projection.class);
        when(reproj.getEpsgCode()).thenReturn("CODE2");
        when(datasource.getReprojections()).thenReturn(List.of(reproj));
        when(map.getData()).thenReturn(List.of(datasource));

        //When
        List<String> projectionSystems = service.getProjectionSystems(map);

        //Then
        assertThat(projectionSystems, hasItems("CODE1", "CODE2"));
    }

    @Test
    public void checkThatCanListProjectionSystemsWithNoReprojections() {
        //Given
        MapDataDefinition map = mock(MapDataDefinition.class);
        DataSource datasource = mock(DataSource.class);
        when(datasource.getEpsgCode()).thenReturn("CODE1");
        when(datasource.getReprojections()).thenReturn(null);
        when(map.getData()).thenReturn(List.of(datasource));

        //When
        List<String> projectionSystems = service.getProjectionSystems(map);

        //Then
        assertThat(projectionSystems, hasItem("CODE1"));
    }

    @Test
    public void checkThatCanGetFavouredProjectionSystem() {
        //Given
        DataSource datasource = mock(DataSource.class);
        when(datasource.getEpsgCode()).thenReturn("default");
        Projection proj1 = mock(Projection.class);
        when(proj1.getEpsgCode()).thenReturn("27700");
        when(datasource.getReprojections()).thenReturn(List.of(proj1));

        //When
        Projection pref = service.getFavouredProjection(datasource, "27700");

        //Then
        assertThat(pref, is(proj1));
    }

    @Test
    public void checkThatCanGetFallBackToDefaultProjectionSystem() {
        //Given
        DataSource datasource = mock(DataSource.class);
        when(datasource.getEpsgCode()).thenReturn("default");

        //When
        Projection pref = service.getFavouredProjection(datasource, "27700");

        //Then
        assertThat(pref, is(datasource));
    }

    @Test
    public void checkThatDefaultsToPrimaryProjectionIfAlternativeWithSameEpsgCodeIsSpecified() {
        //Given
        DataSource datasource = mock(DataSource.class);
        when(datasource.getEpsgCode()).thenReturn("27700");
        Projection proj1 = mock(Projection.class);
        when(proj1.getEpsgCode()).thenReturn("27700");
        when(datasource.getReprojections()).thenReturn(List.of(proj1));

        //When
        Projection pref = service.getFavouredProjection(datasource, "27700");

        //Then
        assertThat(pref, is(datasource));
    }

    @Test
    public void checkThatCanReturnDetailsOnBuckets() {
        //Given
        Bucket b1 = new Bucket();
        b1.setMin(new BigDecimal("0"));
        b1.setMax(new BigDecimal("100"));

        Bucket b2 = new Bucket();
        b2.setMin(new BigDecimal("100"));
        b2.setMax(new BigDecimal("300"));

        List<Bucket> buckets = Arrays.asList(b1, b2);

        //When
        MapBucketDetails details = service.getScaledBuckets(buckets);

        //Then
        assertThat(details.getBuckets(), is(3));
        assertThat(details.getMin(), is(new BigDecimal(0)));
        assertThat(details.getMax(), is(new BigDecimal(300)));
    }

    @Test
    public void checkThatCanGetBucketDetailsWithDecimal() {
        //Given
        Bucket b1 = new Bucket();
        b1.setMin(new BigDecimal("-1.5"));
        b1.setMax(new BigDecimal("3"));

        Bucket b2 = new Bucket();
        b2.setMin(new BigDecimal("3"));
        b2.setMax(new BigDecimal("4.5"));

        List<Bucket> buckets = Arrays.asList(b1, b2);

        //When
        MapBucketDetails details = service.getScaledBuckets(buckets);

        //Then
        assertThat(details.getBuckets(), is(4));
        assertThat(details.getMin(), is(new BigDecimal("-1.5")));
        assertThat(details.getMax(), is(new BigDecimal("4.5")));
    }

    @Test
    public void checkThatCanGetBucketDetailsForSingleBucket() {
        //Given
        Bucket b1 = new Bucket();
        b1.setMin(new BigDecimal("2"));
        b1.setMax(new BigDecimal("5"));

        List<Bucket> buckets = Arrays.asList(b1);

        //When
        MapBucketDetails details = service.getScaledBuckets(buckets);

        //Then
        assertThat(details.getBuckets(), is(1));
        assertThat(details.getMin(), is(new BigDecimal("2")));
        assertThat(details.getMax(), is(new BigDecimal("5")));
    }

    @Test
    public void checkThatCanGenerateScaledBucketsWithIrregularBuckets() {
        //Given
        Bucket b1 = new Bucket();
        b1.setMin(new BigDecimal("0.17"));
        b1.setMax(new BigDecimal("0.53"));

        Bucket b2 = new Bucket();
        b2.setMin(new BigDecimal("0.53"));
        b2.setMax(new BigDecimal("0.69"));


        List<Bucket> buckets = Arrays.asList(b1, b2);

        //When
        MapBucketDetails details = service.getScaledBuckets(buckets);

        //Then
        assertThat(details.getBuckets(), is(4));
        assertThat(details.getMin(), is(new BigDecimal("0.17")));
        assertThat(details.getMax(), is(new BigDecimal("0.69")));
    }

    @Test
    public void checkThatIsSkippedForOpenEndedBuckets() {
        //Given
        Bucket b1 = new Bucket();
        b1.setMax(new BigDecimal("5"));

        List<Bucket> buckets = Arrays.asList(b1);

        //When
        MapBucketDetails details = service.getScaledBuckets(buckets);

        //Then
        assertNull(details);
    }
}
