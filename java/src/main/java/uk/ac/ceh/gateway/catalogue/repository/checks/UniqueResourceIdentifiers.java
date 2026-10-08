package uk.ac.ceh.gateway.catalogue.repository.checks;

import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceIdentifierExistsException;
import uk.ac.ceh.gateway.catalogue.services.ResourceIdentifierLookupService;

/**
 * Refuses a save that gives a document a resource identifier another document already has.
 * An identifier without both a code and a code space (such as the record's own URI) is not
 * checked.
 */
public class UniqueResourceIdentifiers implements SaveCheck {
    private final ResourceIdentifierLookupService resourceIdentifierLookupService;

    public UniqueResourceIdentifiers(ResourceIdentifierLookupService resourceIdentifierLookupService) {
        this.resourceIdentifierLookupService = resourceIdentifierLookupService;
    }

    @Override
    public void check(CatalogueUser user, MetadataDocument incoming, String id, StoredVersion stored) {
        if (incoming.getResourceIdentifiers() == null) return;

        for (ResourceIdentifier ri : incoming.getResourceIdentifiers()) {
            String code = ri.getCode();
            String codeSpace = ri.getCodeSpace();

            if (code == null || codeSpace == null || code.isBlank() || codeSpace.isBlank()) continue;

            String combined = codeSpace + ":" + code;

            resourceIdentifierLookupService.findDocumentIdsByRi(combined).stream()
                .filter(ownerId -> !ownerId.equals(id))
                .findFirst()
                .ifPresent(ownerId -> {
                    throw new ResourceIdentifierExistsException(
                        "A document with Resource Identifier \"" + combined +
                            "\" already exists (id = " + ownerId + "). " +
                            "Resource identifiers must be unique."
                    );
                });
        }
    }
}
