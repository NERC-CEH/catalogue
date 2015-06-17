package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ceh.components.userstore.UserAttribute;
import uk.ac.ceh.components.userstore.UserBuilderFactory;

/**
 * The following class will build domain specific users without contacting any
 * external UserRepository. To do this, all tcext groups which are expected to be 
 * read by this TerraCatalogUserFactory must be registered with there corresponding
 * username suffix (e.g. 'ceh' -> '@ceh.ac.uk')
 * 
 * The Gast user group is preregistered (with no suffix)
 * 
 * @author cjohn
 * @param <U> The User type which this factory builds
 */
public class OfflineTerraCatalogUserFactory<U> implements TerraCatalogUserFactory<U> {
    private final Map<String, String> groupToDomain;
    private final UserBuilderFactory<U> userBuilder;
    
    public OfflineTerraCatalogUserFactory(UserBuilderFactory<U> userBuilder) {
        this(new HashMap<>(), userBuilder);
        groupToDomain.put("gast", "");
    }
    
    protected OfflineTerraCatalogUserFactory(Map<String, String> groupToDomain, UserBuilderFactory<U> userBuilder) {
        this.groupToDomain = groupToDomain;
        this.userBuilder = userBuilder;
    }
    
    public String put(String group, String domainSuffix) {
        return groupToDomain.put(group.toLowerCase(), domainSuffix);
    }
    
    @Override
    public U getAuthor(TerraCatalogExt externalInfo) {
        return userBuilder.newUserBuilder(externalInfo.getOwner())
                   .set(UserAttribute.EMAIL, getEmailAddress(externalInfo))
                   .build();
    }
    
    protected String getEmailAddress(TerraCatalogExt externalInfo) {
        String emailDomain = groupToDomain.get(externalInfo.getOwnerGroup().toLowerCase());
        if(emailDomain == null) {
            throw new IllegalArgumentException("The owner group is unknown : " + externalInfo.getOwnerGroup());
        }
        else {
            return externalInfo.getOwner() + emailDomain;
        }
    }
}
