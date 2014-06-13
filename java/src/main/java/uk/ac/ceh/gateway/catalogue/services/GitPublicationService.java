package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.publication.PublicationException;
import uk.ac.ceh.gateway.catalogue.publication.State;

@Service
public class GitPublicationService implements PublicationService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final StateAssembler stateAssembler;

    @Autowired
    public GitPublicationService(DataRepository<CatalogueUser> repo, DocumentInfoMapper<MetadataInfo> documentInfoMapper, StateAssembler stateAssembler) {
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
        this.stateAssembler = stateAssembler;
    }

    @Override
    public State current(CatalogueUser user, String file) {
        MetadataInfo metadataInfo = getMetadataInfo(file);
        return stateAssembler.toResource(user, file, metadataInfo.getState());
    }

    @Override
    public State transition(CatalogueUser user, String file, String state) {
        MetadataInfo metadataInfo = getMetadataInfo(file);
        metadataInfo.setState(state);
        try {
            repo.submitData(String.format("%s.meta", file), (o)-> documentInfoMapper.writeInfo(metadataInfo, o)).commit(user, "publication state changed");
        } catch (DataRepositoryException ex) {
            throw new PublicationException(String.format("Unable to change publication state to: %s for %s", state, file), ex);
        }
        return new State(state, file);
    }
    
    private MetadataInfo getMetadataInfo(String file) {
        MetadataInfo metadataInfo;
        try {
            metadataInfo = documentInfoMapper.readInfo(repo.getData(String.format("%s.meta", file)).getInputStream());
        } catch (IOException | NullPointerException ex) {
            throw new DocumentDoesNotExistException(String.format("Document: %s does not exist", file), ex);
        }
        return metadataInfo;
    }

}