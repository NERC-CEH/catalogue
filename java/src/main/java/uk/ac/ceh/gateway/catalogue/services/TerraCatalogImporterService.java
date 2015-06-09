package uk.ac.ceh.gateway.catalogue.services;

import lombok.Data;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.OfflineTerraCatalogUserFactory;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.StateTranslatingMetadataInfoFactory;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.TerraCatalogImporter;

/**
 * A factory class which generates terra catalog importers for a given user.
 * @author cjohn
 */
@Data
public class TerraCatalogImporterService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentListingService listingService;
    private final OfflineTerraCatalogUserFactory<CatalogueUser> userFactory;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final StateTranslatingMetadataInfoFactory infoFactory;
        
    public TerraCatalogImporter<MetadataInfo, CatalogueUser> getImporter(CatalogueUser user) {
        return new TerraCatalogImporter<>(
                repo,
                listingService,
                userFactory,
                documentReader,
                documentInfoMapper,
                infoFactory,
                user);
    }
}
