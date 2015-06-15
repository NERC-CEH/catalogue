package uk.ac.ceh.gateway.catalogue.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cjohn
 */
public class DocumentIdentifierServiceTest {
    private DocumentIdentifierService service;
    
    @Before
    public void init() {
        service = new DocumentIdentifierService('-');
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
}
