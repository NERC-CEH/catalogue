package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.apache.solr.client.solrj.RemoteSolrException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingEmbeddingServiceTest {

    @Mock private EmbeddingModel embeddingModel;
    @Mock private SolrClient solrClient;

    private PendingEmbeddingService service;

    private static final int MAX_ATTEMPTS = 3;

    @BeforeEach
    void setup() {
        service = new PendingEmbeddingService(
            embeddingModel, solrClient, Optional.empty(), 50, 0, MAX_ATTEMPTS);
    }

    @Test
    void flushIsNoOpWhenPendingSetIsEmpty() throws Exception {
        service.flush();
        verifyNoInteractions(embeddingModel, solrClient);
    }

    @Test
    void markTwiceWithSameIdResultsInOneEmbeddingCall() throws Exception {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f, 0.2f});

        SolrIndex idx = new SolrIndex().setIdentifier("doc-1").setTitle("River flow");
        service.mark("doc-1", idx);
        service.mark("doc-1", idx);  // second mark — same ID, should be deduplicated

        service.flush();

        verify(embeddingModel, times(1)).embed(any(String.class));
    }

    @Test
    void flushConstructsPartialAtomicUpdateWithCorrectStructure() throws Exception {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f, 0.2f, 0.3f});

        SolrIndex idx = new SolrIndex().setIdentifier("abc-123").setTitle("Soil carbon");
        service.mark("raw-id", idx);
        service.flush();

        ArgumentCaptor<SolrInputDocument> docCaptor = ArgumentCaptor.forClass(SolrInputDocument.class);
        verify(solrClient).add(eq("documents"), docCaptor.capture());

        SolrInputDocument update = docCaptor.getValue();
        assertThat(update.getFieldValue("identifier")).isEqualTo("abc-123");

        Object vectorField = update.getFieldValue("vector");
        assertThat(vectorField).isInstanceOf(Map.class);
        assertThat(vectorField.toString()).contains("set");

        Object embeddingTextField = update.getFieldValue("embedding_text");
        assertThat(embeddingTextField).isInstanceOf(Map.class);
        assertThat(embeddingTextField.toString()).contains("set");
    }

    @Test
    void flushCommitsSolrAfterProcessing() throws Exception {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        service.flush();

        verify(solrClient).commit("documents");
    }

    @Test
    void flushReQueuesIdWhenEmbeddingFails() throws Exception {
        given(embeddingModel.embed(any(String.class))).willThrow(new RuntimeException("Bedrock unavailable"));

        SolrIndex idx = new SolrIndex().setIdentifier("doc-1").setTitle("Test");
        service.mark("doc-1", idx);
        service.flush();

        // After the failed flush, the ID should still be re-queued
        // Call flush again — it should attempt Bedrock again
        service.flush();
        verify(embeddingModel, times(2)).embed(any(String.class));
    }

    @Test
    void buildEmbeddingTextIncludesSemanticFields() {
        SolrIndex idx = new SolrIndex()
            .setTitle("River flow monitoring")
            .setDescription("Long-term discharge measurements")
            .setKeyword(List.of("hydrology", "river"))
            .setAltTitle(List.of("Streamflow"))
            .setObjectives("Understand catchment response")
            .setObservedPropertyTitle(List.of("discharge", "water level"))
            .setKeywordsParameters(List.of("flow rate"))
            .setSupplementalDescription(List.of("Additional context"));

        String text = service.buildEmbeddingText(idx);

        assertThat(text).contains("River flow monitoring");
        assertThat(text).contains("Long-term discharge measurements");
        assertThat(text).contains("hydrology");
        assertThat(text).contains("Streamflow");
        assertThat(text).contains("Understand catchment response");
        assertThat(text).contains("discharge");
        assertThat(text).contains("flow rate");
        assertThat(text).contains("Additional context");
    }

    @Test
    void buildEmbeddingTextExcludesAdministrativeFields() {
        SolrIndex idx = new SolrIndex()
            .setTitle("River flow")
            .setState("published")
            .setCatalogue("eidc")
            .setLicence("OGL")
            .setResourceType("Dataset")
            .setView(List.of("public"));

        String text = service.buildEmbeddingText(idx);

        assertThat(text).contains("River flow");
        assertThat(text).doesNotContain("published");
        assertThat(text).doesNotContain("eidc");
        assertThat(text).doesNotContain("OGL");
        assertThat(text).doesNotContain("Dataset");
        assertThat(text).doesNotContain("public");
    }

    @Test
    void buildEmbeddingTextHandlesNullAndEmptyFieldsGracefully() {
        SolrIndex idx = new SolrIndex()
            .setTitle("Minimal record")
            .setDescription(null)
            .setKeyword(null)
            .setAltTitle(List.of());

        String text = service.buildEmbeddingText(idx);

        assertThat(text).isEqualTo("Minimal record");
    }

    @Test
    void buildEmbeddingTextIncludesTemporalExtentText() {
        SolrIndex idx = new SolrIndex()
                .setTitle("River monitoring")
                .setTemporalExtentText("Data collected from 1990 to 2020");

        String text = service.buildEmbeddingText(idx);

        assertThat(text).contains("Data collected from 1990 to 2020");
    }

    @Test
    void documentExtractorTextAppendedToEmbedding() throws Exception {
        //Given — service with a mock extractor
        SupportingDocumentExtractor extractor = mock(SupportingDocumentExtractor.class);
        given(extractor.extractText("doc-id")).willReturn("peat bog carbon flux methodology");
        PendingEmbeddingService serviceWithExtractor = new PendingEmbeddingService(
                embeddingModel, solrClient, Optional.of(extractor), 50, 0, MAX_ATTEMPTS);

        SolrIndex idx = new SolrIndex().setTitle("Peat study");
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});

        serviceWithExtractor.mark("doc-id", idx);
        serviceWithExtractor.flush();

        //Then — embedding was called with combined text containing doc text
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(embeddingModel).embed(textCaptor.capture());
        assertThat(textCaptor.getValue()).contains("peat bog carbon flux methodology");
        assertThat(textCaptor.getValue()).contains("Peat study");
    }

    @Test
    void documentTextFieldSetInSolrUpdateWhenExtractorPresent() throws Exception {
        SupportingDocumentExtractor extractor = mock(SupportingDocumentExtractor.class);
        given(extractor.extractText("doc-id")).willReturn("carbon flux methodology");
        PendingEmbeddingService serviceWithExtractor = new PendingEmbeddingService(
                embeddingModel, solrClient, Optional.of(extractor), 50, 0, MAX_ATTEMPTS);

        SolrIndex idx = new SolrIndex().setIdentifier("doc-id").setTitle("Peat study");
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});

        serviceWithExtractor.mark("doc-id", idx);
        serviceWithExtractor.flush();

        ArgumentCaptor<SolrInputDocument> docCaptor = ArgumentCaptor.forClass(SolrInputDocument.class);
        verify(solrClient).add(eq("documents"), docCaptor.capture());

        // document_text must be SET in every atomic update — it is stored=false with no copyField,
        // so Solr cannot read it back; omitting it would silently wipe BM25 keyword coverage
        Object documentTextField = docCaptor.getValue().getFieldValue("document_text");
        assertThat(documentTextField).isInstanceOf(Map.class);
        assertThat(documentTextField.toString()).contains("carbon flux methodology");
    }

    @Test
    void documentTextFieldAbsentWhenNoExtractorText() throws Exception {
        SolrIndex idx = new SolrIndex().setIdentifier("doc-1").setTitle("Baseline");
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});

        service.mark("doc-1", idx);
        service.flush();

        ArgumentCaptor<SolrInputDocument> docCaptor = ArgumentCaptor.forClass(SolrInputDocument.class);
        verify(solrClient).add(eq("documents"), docCaptor.capture());

        assertThat(docCaptor.getValue().getField("document_text")).isNull();
    }

    @Test
    void documentExtractorAbsentBehaviourUnchanged() throws Exception {
        //Given — service with no extractor (the default setup)
        SolrIndex idx = new SolrIndex().setTitle("Baseline study");
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.2f});

        service.mark("base-id", idx);
        service.flush();

        //Then — embedding called with metadata-only text, no extractor interactions
        verify(embeddingModel).embed(any(String.class));
    }

    // --- Failure handling: nothing silent, nothing endless, nothing dropped ---

    @Test
    @DisplayName("A document that always fails is abandoned instead of retried forever")
    void permanentFailureIsAbandonedAfterMaxAttempts() throws Exception {
        given(embeddingModel.embed(any(String.class))).willThrow(new RuntimeException("unembeddable"));

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        for (int i = 0; i < MAX_ATTEMPTS + 3; i++) {
            service.flush();
        }

        // Tried exactly the budget, then stopped — previously every flush for the life of the
        // process spent another embedding call on it.
        verify(embeddingModel, times(MAX_ATTEMPTS)).embed(any(String.class));
        assertThat(service.abandonedEmbeddings()).containsKey("doc-1");
        assertThat(service.abandonedEmbeddings().get("doc-1")).contains("unembeddable");
    }

    @Test
    @DisplayName("A document Solr rejects with 4xx is abandoned without retrying")
    void solrRejectionIsNotRetried() throws Exception {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(solrClient.add(eq("documents"), any(SolrInputDocument.class)))
            .willThrow(new RemoteSolrException("http://solr:8983/solr", 400, "unknown field", null));

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        service.flush();
        service.flush();

        // A 400 is deterministic, so the retry budget would only repeat it.
        verify(embeddingModel, times(1)).embed(any(String.class));
        assertThat(service.abandonedEmbeddings()).containsKey("doc-1");
    }

    @Test
    @DisplayName("An unreachable backend keeps retrying rather than spending the document's budget")
    void outageDoesNotConsumeAttempts() throws Exception {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(solrClient.add(eq("documents"), any(SolrInputDocument.class)))
            .willThrow(new SolrServerException("connection refused"));

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        int flushes = MAX_ATTEMPTS + 3;
        for (int i = 0; i < flushes; i++) {
            service.flush();
        }

        // Still queued well past the budget: an outage must not be mistaken for a bad document, or
        // downtime longer than maxAttempts flushes would silently discard every pending embedding.
        verify(embeddingModel, times(flushes)).embed(any(String.class));
        assertThat(service.abandonedEmbeddings()).isEmpty();
    }

    @Test
    @DisplayName("A failed commit re-queues the documents it did not make searchable")
    void failedCommitReQueuesTheBatch() throws Exception {
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        given(solrClient.commit("documents")).willThrow(new SolrServerException("commit failed"));

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        service.flush();
        service.flush();

        // The adds had succeeded but were not searchable, and the flush had already removed them
        // from the pending map, so without re-queuing the embedding was lost behind one warning.
        verify(embeddingModel, times(2)).embed(any(String.class));
        verify(solrClient, times(2)).commit("documents");
    }

    @Test
    @DisplayName("Nothing is committed when every document in the flush failed")
    void noCommitWhenNothingWasAdded() throws Exception {
        given(embeddingModel.embed(any(String.class))).willThrow(new RuntimeException("unembeddable"));

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        service.flush();

        verify(solrClient, never()).commit("documents");
    }

    @Test
    @DisplayName("A success clears the failure count so earlier failures do not accumulate")
    void successResetsTheAttemptCount() throws Exception {
        given(embeddingModel.embed(any(String.class)))
            .willThrow(new RuntimeException("transient"))
            .willReturn(new float[]{0.1f})
            .willThrow(new RuntimeException("transient"))
            .willThrow(new RuntimeException("transient"));

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        for (int i = 0; i < 4; i++) {
            service.flush();
        }

        // Four flushes, three failures, but never three *consecutive* ones, so it is still in play.
        assertThat(service.abandonedEmbeddings()).isEmpty();
    }

    @Test
    @DisplayName("An abandoned document returns to normal service once it succeeds again")
    void abandonedDocumentClearsOnLaterSuccess() throws Exception {
        given(embeddingModel.embed(any(String.class))).willThrow(new RuntimeException("unembeddable"));

        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.flush();
        }
        assertThat(service.abandonedEmbeddings()).containsKey("doc-1");

        reset(embeddingModel);
        given(embeddingModel.embed(any(String.class))).willReturn(new float[]{0.1f});
        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        service.flush();

        assertThat(service.abandonedEmbeddings()).isEmpty();
    }

    // ------------------------------------------------------- coverage for the maintenance page

    private void givenSolrCounts(long total, long embedded) throws Exception {
        given(solrClient.query(eq("documents"), any(org.apache.solr.client.solrj.request.SolrQuery.class)))
            .willAnswer(invocation -> {
                String q = invocation.getArgument(1, org.apache.solr.client.solrj.request.SolrQuery.class).getQuery();
                var results = new org.apache.solr.common.SolrDocumentList();
                results.setNumFound("*:*".equals(q) ? total : embedded);
                var response = mock(org.apache.solr.client.solrj.response.QueryResponse.class);
                given(response.getResults()).willReturn(results);
                return response;
            });
    }

    @Test
    @DisplayName("Coverage reports how many records carry a vector")
    void coverageCountsEmbeddedRecords() throws Exception {
        givenSolrCounts(1991, 1847);

        var coverage = service.coverage();

        assertThat(coverage.total()).isEqualTo(1991);
        assertThat(coverage.embedded()).isEqualTo(1847);
    }

    @Test
    @DisplayName("Coverage reports what is still queued and what has been given up on")
    void coverageReportsQueueAndAbandoned() throws Exception {
        givenSolrCounts(3, 1);
        given(embeddingModel.embed(any(String.class))).willThrow(new RuntimeException("unembeddable"));
        service.mark("doc-1", new SolrIndex().setIdentifier("doc-1").setTitle("Test"));
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.flush();
        }
        service.mark("doc-2", new SolrIndex().setIdentifier("doc-2").setTitle("Queued"));

        var coverage = service.coverage();

        assertThat(coverage.pending()).isEqualTo(1);
        assertThat(coverage.abandoned()).isEqualTo(1);
    }

    /**
     * The vector field is a DenseVectorField, which Solr will not accept a wildcard or range query
     * against, so embedding_text stands in for it -- the two are written by the same atomic update.
     */
    @Test
    @DisplayName("Coverage counts embedding_text, which Solr can query, not the vector field")
    void coverageQueriesEmbeddingTextNotTheVectorField() throws Exception {
        givenSolrCounts(10, 5);

        service.coverage();

        ArgumentCaptor<org.apache.solr.client.solrj.request.SolrQuery> queries =
            ArgumentCaptor.forClass(org.apache.solr.client.solrj.request.SolrQuery.class);
        verify(solrClient, times(2)).query(eq("documents"), queries.capture());
        assertThat(queries.getAllValues()).extracting(org.apache.solr.client.solrj.request.SolrQuery::getQuery)
            .containsExactlyInAnyOrder("*:*", "embedding_text:[* TO *]");
    }
}
