package uk.ac.ceh.gateway.catalogue.wms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TMSToWMSGetMapServiceTest {
    private TMSToWMSGetMapService service;

    @BeforeEach
    public void createService() {
        service = new TMSToWMSGetMapService();
    }

    @Test
    public void checkFullBoundingBox() {
        //Given
        int z = 0, x = 0, y = 0;

        //When
        String bbox = service.getBBoxParam(z, x, y);

        //Then
        assertThat("Expected same bbox", bbox, equalTo("-20037508.34,-20037508.34,20037508.34,20037508.34"));
    }


    @Test
    public void checkBBoxTopLeftZoom1() {
        //Given
        int z=1, x=0, y=1;

        //When
        String bbox = service.getBBoxParam(z, x, y);

        //Then
        assertThat("Expected same bbox", bbox, equalTo("-20037508.34,0,0,20037508.34"));

    }

    @Test
    public void checkBBoxTileOnZoom2() {
        //Given
        int z=2, x=1, y=2;

        //When
        String bbox = service.getBBoxParam(z, x, y);

        //Then
        assertThat("Expected same bbox", bbox, equalTo("-10018754.17,0,0,10018754.17"));

    }

    @Test
    public void checkThatServiceGeneratesCorrectBBox() {
        //Given
        int z = 6, x = 31, y= 43;

        //When
        String bbox = service.getBBoxParam(z, x, y);

        //Then
        assertThat("Expected same bbox", bbox, equalTo("-626172.14,6887893.49,-0,7514065.63"));
    }

    @Test
    public void checkCanAppendQuestionMarkOnInvalidWMS() {
        //Given
        String invalidWMS = "http://www.google.com/wms";

        //When
        String valid = service.convertToValidWMSEndpoint(invalidWMS);

        //Then
        assertThat("Expected a question mark to be apppended", valid, equalTo("http://www.google.com/wms?"));
    }

    @Test
    public void checkCanAppendAmpersandMarkOnInvalidWMS() {
        //Given
        String invalidWMS = "http://www.google.com/wms?myCustom=param";

        //When
        String valid = service.convertToValidWMSEndpoint(invalidWMS);

        //Then
        assertThat("Expected a ampersand mark to be apppended", valid, equalTo("http://www.google.com/wms?myCustom=param&"));
    }

    @Test
    public void checkValidWMSWithNoParamsDoesntChange() {
        //Given
        String valid = "http://www.google.com/wms?";

        //When
        String validated = service.convertToValidWMSEndpoint(valid);

        //Then
        assertThat("Expected wms to not change", validated, equalTo(valid));
    }

    @Test
    public void checkValidWMSWithParamsDoesntChange() {
        //Given
        String valid = "http://www.google.com/wms?param=value&";

        //When
        String validated = service.convertToValidWMSEndpoint(valid);

        //Then
        assertThat("Expected wms to not change", validated, equalTo(valid));
    }
}
