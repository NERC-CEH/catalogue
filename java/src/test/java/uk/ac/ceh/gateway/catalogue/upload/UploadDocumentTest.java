package uk.ac.ceh.gateway.catalogue.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class UploadDocumentTest {

    @Test
    public void create() {
        //given
        val id = "11-22-33-44-55";
        val dropboxNode = create("hubbub-dropbox-data-true-response.json");
        val datastoreNode = create("hubbub-eidchub-data-true-response.json");
        val supportingDocumentsNode = create("hubbub-supporting-documents-data-true-response.json");

        //when
        val actual = new UploadDocument(id, dropboxNode, datastoreNode, supportingDocumentsNode);

        //then
        assertThat(actual.getId(), equalTo(id));
        assertThat(actual.getUploadFiles().keySet(), containsInAnyOrder("documents", "datastore", "supporting-documents"));

        val documents = actual.getUploadFiles().get("documents");
        assertThat(documents.getDocuments().size(), equalTo(0));

        val datastore = actual.getUploadFiles().get("datastore");
        assertThat(datastore.getDocuments().size(), equalTo(2));
        assertThat(datastore.getInvalid().size(), equalTo(0));
        assertThat(datastore.getDocuments().keySet(), containsInAnyOrder(
                "/eidchub/a4192575-e91a-477d-8f64-aae3b32faf7a/CBESS_Eddy_Covariance_data_Cartmel_Sands.csv",
                "/eidchub/a4192575-e91a-477d-8f64-aae3b32faf7a/CBESS_data_Cartmel_Sands.csv"
        ));

        val supportingDocuments = actual.getUploadFiles().get("supporting-documents");
        assertThat(supportingDocuments.getDocuments().size(), equalTo(1));
        assertThat(supportingDocuments.getInvalid().size(), equalTo(0));
        assertThat(supportingDocuments.getDocuments().keySet(), containsInAnyOrder(
                "/supporting-documents/a4192575-e91a-477d-8f64-aae3b32faf7a/CBESS_Eddy_Covariance_data_Cartmel_Sands_supporting-document.docx"
        ));
    }

    @SneakyThrows
    private JsonNode create(String filename) {
        val objectMapper = new ObjectMapper();
        return objectMapper.readTree(getClass().getResourceAsStream(filename));
    }
}