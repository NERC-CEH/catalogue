package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.val;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileInfoTest {

    @Test
    public void correctUrlForFileWithSpacesInName() {
        //given
        val id = "993c5778-e139-4171-a57f-7a0f396be4b8";
        val filename = "name with spaces.csv";

        //when
        val actual = new UploadController.FileInfo(id, filename);

        //then
        assertThat(actual.getName(), equalTo(filename));
        assertThat(
                actual.getDeleteUrl(),
                equalTo("/documents/" + id + "/delete-upload-file/name%20with%20spaces.csv")
        );
    }
}
