package uk.ac.ceh.gateway.catalogue.upload;

import lombok.val;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.ac.ceh.gateway.catalogue.upload.HubbubResponse.*;

public class HubbubResponseTest {

    @Test
    public void truncatedName() {
        //given
        val fileInfo = new FileInfo(
                0L,
                "",
                "",
                "",
                "",
                "",
                "",
                "/eidchub/444bd227-b934-412e-8863-326afb77063b/pet/data.csv",
                "",
                "",
                0L
        );
        
        //when
        val actual = fileInfo.getTruncatedPath();
        
        //then
        assertThat(actual, equalTo("pet/data.csv"));
    }
}