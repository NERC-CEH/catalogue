package uk.ac.ceh.gateway.catalogue.services;

import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DepositRequestDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@AllArgsConstructor
public class DepositRequestService {

    private final DocumentRepository documentRepository;

    public void save(CatalogueUser user, DepositRequestDocument depositRequest) {
        try {
            documentRepository.saveNew(user, depositRequest, "DEPOSIT_REQUEST_DOCUMENT", "new deposit request");
        } catch(DocumentRepositoryException err) {
            throw new RuntimeException(err);
        }
    }

    public DepositRequestDocument get(String guid) {
        try {
            val document = documentRepository.read(guid);
            return (DepositRequestDocument) document;
        } catch(DocumentRepositoryException err) {
            throw new RuntimeException(err);
        }
    }

}
