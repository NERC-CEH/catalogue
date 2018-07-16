package uk.ac.ceh.gateway.catalogue.file;

import java.io.IOException;

public interface FileRepository {
    String create(String data) throws IOException;
    String read(String id) throws UnknownFileException, IOException;
    void update(String id, String data);
    void delete(String id);
}
