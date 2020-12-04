package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.val;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse.FileInfo;
import static uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse.Pagination;

public class UploadDocumentTest {

    @Test
    public void create() {
        //given
        val id = "11-22-33-44-55";
        val dropboxResponse = new HubbubResponse(Collections.emptyList(), new Pagination(1,20, 2));
        val datastoreResponse = new HubbubResponse(datastore(), new Pagination(1,20, 2));
        val supportingDocumentsResponse = new HubbubResponse(supportingDocuments(), new Pagination(1,20, 3));

        //when
        val actual = new UploadDocument(id, dropboxResponse, datastoreResponse, supportingDocumentsResponse);

        //then
        assertThat(actual.getId(), equalTo(id));
        assertThat(actual.getUploadFiles().keySet(), containsInAnyOrder("documents", "datastore", "supporting-documents"));

        val documents = actual.getUploadFiles().get("documents");
        assertThat(documents.getDocuments().size(), equalTo(0));

        val datastore = actual.getUploadFiles().get("datastore");
        assertThat(datastore.getDocuments().size(), equalTo(2));
        assertThat(datastore.getInvalid().size(), equalTo(0));
        assertThat(datastore.getDocuments().keySet(), containsInAnyOrder(
                "/sdfsdfsdf/dataset0.csv",
                "/sdfsdfsdf/dataset1.csv"
        ));

        val supportingDocuments = actual.getUploadFiles().get("supporting-documents");
        assertThat(supportingDocuments.getDocuments().size(), equalTo(2));
        assertThat(supportingDocuments.getInvalid().size(), equalTo(1));
        assertThat(supportingDocuments.getDocuments().keySet(), containsInAnyOrder(
                "/sdfsdfsdf/support0.csv",
                "/sdfsdfsdf/support1.csv"
        ));
        assertThat(supportingDocuments.getInvalid().keySet(), containsInAnyOrder(
                "/sdfsdfsdf/support2.csv"
        ));
    }

    private List<FileInfo> datastore() {
        val f0 = new FileInfo(123123L, "csv", "dc0834234", "dataset0.csv", "text/csv", "192032.837", "dataset 0.csv","/sdfsdfsdf/dataset0.csv","\\\\mnt\\\\\\\\eidchub\\\\\\something","VALID", 1602301536812L);
        val f1 = new FileInfo(123123L, "csv", "dfjf459df", "dataset1.csv", "text/csv", "192032.837", "dataset 1.csv","/sdfsdfsdf/dataset1.csv","\\\\mnt\\\\\\\\eidchub\\\\\\something","VALID", 1602301536812L);
        return Arrays.asList(f0, f1);
    }

    private List<FileInfo> supportingDocuments() {
        val f0 = new FileInfo(123123L, "csv", "dc0834234", "support0.csv", "text/csv", "192032.837", "support 0.csv","/sdfsdfsdf/support0.csv","\\\\mnt\\\\\\\\eidchub\\\\\\something","VALID", 1602301536812L);
        val f1 = new FileInfo(123123L, "csv", "dc0834234", "support1.csv", "text/csv", "192032.837", "support 1.csv","/sdfsdfsdf/support1.csv","\\\\mnt\\\\\\\\eidchub\\\\\\something","VALID", 1602301536812L);
        val f2 = new FileInfo(123123L, "csv", "dc0834234", "support2.csv", "text/csv", "192032.837", "support 1.csv","/sdfsdfsdf/support2.csv","\\\\mnt\\\\\\\\eidchub\\\\\\something","INVALID", 1602301536812L);
        return Arrays.asList(f0, f1, f2);
    }
}