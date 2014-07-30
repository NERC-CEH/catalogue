package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import static java.lang.String.format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Service("permission")
public class PermissionService {
    private final DataRepository<?> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;

    @Autowired
    public PermissionService(DataRepository<?> repo, DocumentInfoMapper documentInfoMapper) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
    }
    
    public boolean toAccess(CatalogueUser user, String file, String revision, String permission) throws IOException {
        MetadataInfo metadataInfo = documentInfoMapper.readInfo(repo.getData(revision, format("%s.meta", file)).getInputStream());
        
        return metadataInfo != null && userCanAccess(user, metadataInfo);
        
    }
    
    private boolean userCanAccess(CatalogueUser user, MetadataInfo metadataInfo) {
        return user.isPublic() && metadataInfo.getState().equalsIgnoreCase("public");
    }

}