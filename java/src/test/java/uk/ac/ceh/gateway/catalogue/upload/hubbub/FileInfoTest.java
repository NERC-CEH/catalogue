package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class FileInfoTest {
    @Test
    void truncatedName() {
        //given
        val fileInfo = new FileInfo(
            0L,
            "",
            "",
            "/eidchub/444bd227-b934-412e-8863-326afb77063b/pet/data.csv",
            "",
            0L
        );

        //when
        val actual = fileInfo.getTruncatedPath();

        //then
        assertThat(actual, equalTo("pet/data.csv"));
    }

    @Test
    @SneakyThrows
    void createFromJsonNodeWithNoHash() {
        //given
        val json = "{\"bytes\":41896,\"name\":\"test-2.csv\",\"path\":\"/dropbox/123456/test-2.csv\",\"status\":\"WRITING\",\"time\":\"16274803242\"}";
        val mapper = new ObjectMapper();
        val node = mapper.readTree(json);

        //when
        val fileInfo = new FileInfo(node);

        //then
        assertThat(fileInfo.getHash(), equalTo(""));
    }

}
