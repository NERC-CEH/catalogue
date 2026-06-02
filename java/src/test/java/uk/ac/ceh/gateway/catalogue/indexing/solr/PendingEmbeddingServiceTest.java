package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

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

    @BeforeEach
    void setup() {
        service = new PendingEmbeddingService(embeddingModel, solrClient, Optional.empty(), 50, 0);
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
                embeddingModel, solrClient, Optional.of(extractor), 50, 0);

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
                embeddingModel, solrClient, Optional.of(extractor), 50, 0);

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
}
