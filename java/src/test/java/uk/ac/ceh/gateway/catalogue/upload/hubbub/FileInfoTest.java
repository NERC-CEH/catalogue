package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileInfoTest {
    @Test
    public void truncatedName() {
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
}
