package uk.ac.ceh.gateway.catalogue.file;

import org.springframework.core.io.WritableResource;

public interface ResourceCreator {
    WritableResource create(String location);
}
