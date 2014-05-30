package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.UserBuilderFactory;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

/**
 *
 * @author cjohn
 */
public class OfflineTerraCatalogUserFactoryTest {
    UserBuilderFactory<CatalogueUser> userBuilderFactory;
    
    
    @Before
    public void createUserBuilderFactory() {
        userBuilderFactory = new AnnotatedUserHelper(CatalogueUser.class);
    }
    
    @Test
    public void checkThatWeCanAddAdditionalGroupSuffixes() {
        //Given
        Map<String,String> groupToDomain = new HashMap<>();
        OfflineTerraCatalogUserFactory factory = new OfflineTerraCatalogUserFactory(groupToDomain, userBuilderFactory);
        String group = "my group";
        String myDomain = "@group.com";
        
        //When
        factory.put(group, myDomain);
        
        //Then
        assertTrue("Expected to find an entry for group", groupToDomain.containsKey(group));
        assertEquals("Expected the same domain for group", myDomain, groupToDomain.get(group));
    }
    
    @Test
    public void checkThatTCGastGroupEmailIsSameAsUsername() {
        //Given
        OfflineTerraCatalogUserFactory factory = new OfflineTerraCatalogUserFactory(userBuilderFactory);
        
        TerraCatalogExt ext = mock(TerraCatalogExt.class);
        when(ext.getOwnerGroup()).thenReturn("gast");
        when(ext.getOwner()).thenReturn("mr@owner.ac.uk");
        
        //When
        String email = factory.getEmailAddress(ext);
        
        //Then
        assertEquals("Expected the same email address as the supplied username", "mr@owner.ac.uk", email);
    }
    
    @Test
    public void checkThatCustomGroupReturnsEmailConcatenation() {
        //Given
        OfflineTerraCatalogUserFactory factory = new OfflineTerraCatalogUserFactory(userBuilderFactory);
        factory.put("customGroup", "@bob.com");
        
        TerraCatalogExt ext = mock(TerraCatalogExt.class);
        when(ext.getOwnerGroup()).thenReturn("customGroup");
        when(ext.getOwner()).thenReturn("mr");
        
        //When
        String email = factory.getEmailAddress(ext);
        
        //Then
        assertEquals("Expected the concatenation of username and email", "mr@bob.com", email);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void expectExceptionIfLookingUpUnRegistedGroup() {
        //Given
        OfflineTerraCatalogUserFactory factory = new OfflineTerraCatalogUserFactory(userBuilderFactory);
        TerraCatalogExt ext = mock(TerraCatalogExt.class);
        when(ext.getOwnerGroup()).thenReturn("not registered hehehe");
        when(ext.getOwner()).thenReturn("mr");
        
        //When
        String email = factory.getEmailAddress(ext);
        
        //Then
        fail("Expected an illegal argument exception");
    }
    
    @Test
    public void expectUserToBeBuiltWithEmailAddressAttached() {
        //Given
        OfflineTerraCatalogUserFactory<CatalogueUser> factory = new OfflineTerraCatalogUserFactory<>(userBuilderFactory);
        factory.put("custom", "@email.com");
        
        TerraCatalogExt ext = mock(TerraCatalogExt.class);
        when(ext.getOwnerGroup()).thenReturn("custom");
        when(ext.getOwner()).thenReturn("mr");

        //When
        CatalogueUser user = factory.getAuthor(ext);
        
        //Then
        assertEquals("Expected the same username", "mr", user.getUsername());
        assertEquals("Expected the generated email", "mr@email.com", user.getEmail());
    }
}

