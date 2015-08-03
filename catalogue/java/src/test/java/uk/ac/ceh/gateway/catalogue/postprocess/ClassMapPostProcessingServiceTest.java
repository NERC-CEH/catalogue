package uk.ac.ceh.gateway.catalogue.postprocess;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;

/**
 *
 * @author cjohn
 */
public class ClassMapPostProcessingServiceTest {
    @Test
    public void checkThatDelegatesToClassMapPostProcessingService() throws PostProcessingException {
        //Given
        PostProcessingService delegatee = mock(PostProcessingService.class);
        ClassMap<PostProcessingService> map = mock(ClassMap.class);
        when(map.get(String.class)).thenReturn(delegatee);
        ClassMapPostProcessingService service = new ClassMapPostProcessingService(map);
        
        //When
        service.postProcess("Try do something with this string");
        
        //Then
        ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
        verify(delegatee).postProcess(capture.capture());
        
        assertThat(capture.getValue(), equalTo("Try do something with this string"));
    }
    
    @Test
    public void checkThatDoesNothingIfNoMatchFound() throws PostProcessingException {
        //Given
        ClassMap<PostProcessingService> map = mock(ClassMap.class);
        ClassMapPostProcessingService service = new ClassMapPostProcessingService(map);
        
        //When
        service.postProcess("Nothing should happen");
        
        //Then
        // Didn't fail with exception
    }
}
