package uk.ac.ceh.gateway.catalogue.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


public class DocumentIdentifierServiceTest {
    private DocumentIdentifierService service;
    private Catalogue ceh;
    
    @BeforeEach
    public void init() {
        service = new DocumentIdentifierService("https://catalogue.ceh.ac.uk", '-');
    }
    
    @Test
    public void checkThatRemovesDodgyCharacters() {
        //Given
        String threddsId = "CHESSModel/DetailWhole.ncml";
        
        //When
        String validId = service.generateFileId(threddsId);
        
        //Then
        assertThat(validId, equalTo("CHESSModel-DetailWhole-ncml"));
    }
    
    @Test
    public void checkThatValidIdDoesntChange() {
        //Given
        String guid = "44020f7b-ba7a-44f9-952b-08af0e479615";
        
        //When
        String validId = service.generateFileId(guid);
        
        //Then
        assertThat(validId, equalTo(guid));
    }
    
    @Test
    public void checkThatNullYieldNull() {
        //Given
        String id = null;
        
        //When
        String fileId = service.generateFileId(id);
        
        //Then
        assertNull(fileId);
    }
    
    @Test
    public void checkThatGeneratesIdInCorrectFormat() {
        //Given
        String id = "myPath";
        
        //When
        String url = service.generateUri(id);
        
        //Then
        assertThat(url, equalTo("https://catalogue.ceh.ac.uk/id/myPath"));
    }
    
    @Test
    public void checkThatFailsWhenCreatingAUriWithoutAnId() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            //Given
            String id = null;

            //When
            String uri = service.generateUri(id);

            //Then
            fail("Expected to fail");
        });
    }
    
    @Test
    public void checkThatGeneratesHistoricIdInCorrectFormat() {
        //Given
        String id = "myPath";
        String revision = "revision";
        
        //When
        String url = service.generateUri(id, revision);
        
        //Then
        assertThat(url, equalTo("https://catalogue.ceh.ac.uk/history/revision/myPath"));
    }
}
