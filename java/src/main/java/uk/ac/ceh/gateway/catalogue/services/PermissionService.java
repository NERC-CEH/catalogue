package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.User;
import uk.ac.ceh.gateway.catalogue.services.UserMembershipService.UserMembership;

/**
 * The following service is responsible for deciding which permissions a given
 * user has for a given file or file/revision pair. This is determined from the 
 * content of the document which should be stored in the git data repository.
 * 
 * There are also helper methods provided which are intended to be called from
 * the PreAuthorize annotation.
 * @author Christopher Johnson
 */
@Service("permission")
public class PermissionService {
    private final DataRepository<User> repo;
    private final JsonObjectMapperService json;
    private final UserMembershipService userMembershipService;
    
    @Autowired
    public PermissionService(
            DataRepository<User> repo,
            JsonObjectMapperService json,
            UserMembershipService userMembershipService) {
        this.repo = repo;
        this.json = json;
        this.userMembershipService = userMembershipService;
    }
    
    public boolean toAccess(String filename, String permission) throws IOException, DataRepositoryException {
        Metadata metadata = json.read(repo.getData(filename), Metadata.class);
        return toAccess(metadata.getInfo(), permission);
    }
    
    public boolean toAccess(String filename, String revision, String permission) throws IOException, DataRepositoryException {
        Metadata metadata = json.read(repo.getData(filename, revision), Metadata.class);
        return toAccess(metadata.getInfo(), permission);
    }
    
    public boolean toAccess(MetadataInfo informationForDocument, String permission) throws IOException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof User) {
            return getDocumentPermissions((User)principal, informationForDocument
                    ).contains(Permission.valueOf(permission));
        }
        else {
            throw new IllegalArgumentException("The provided types where incorrect");
        }
    }
    
    /**
     * The following method will determine the permissions the given user has for
     * documents which are held in the UKEOF git data repository.
     * @see <a href="https://wiki.ceh.ac.uk/display/ukeof/Document+Permissions">Document Permissions</a>
     * @param user The user to get the permissions for
     * @param informationForDocument of the file to check permissions of
     * @return A set of permissions for the given file and given user
     * @throws IOException if the info document could not be read
     */
    public Set<Permission> getDocumentPermissions(User user, MetadataInfo informationForDocument) throws IOException {
        UserMembership membership = userMembershipService.getMembership(user);
        
        //Determine if the current user has full permissions for this document
        if( membership.isAllowedToWriteDocumentFor(informationForDocument.getOrganisation())) {
            return EnumSet.allOf(Permission.class); //user has full permissions, return now with out futher processing
        }
        
        //Handle users without full permissions. Start with an INFO_READ, everyone can do this
        EnumSet<Permission> toReturn = EnumSet.of(Permission.INFO_READ); 
        
        if(membership.isUserAllowedToReadAllDocuments()){
            toReturn.addAll(EnumSet.of(Permission.DOCUMENT_READ, Permission.SENSITIVE_READ));
        }
        
        if(informationForDocument.getState().equals(MetadataInfo.State.PUBLIC)) {
            toReturn.add(Permission.DOCUMENT_READ);
        }
        
        if(informationForDocument.getState().equals(MetadataInfo.State.SENSITIVE) && membership.isAuthenticated()) {
            toReturn.add(Permission.DOCUMENT_READ);
        }
        
        return toReturn;
    }
}