package uk.ac.ceh.gateway.catalogue.file;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static java.lang.String.format;

@AllArgsConstructor
public class FilesystemRepository implements FileRepository {
    private final ResourceLoader resourceLoader;
    private final String datastore;
    private final ResourceCreator resourceCreator;
    private final Updater updater;

    @Override
    public String create(@NonNull String data) throws IOException {
        val id = UUID.randomUUID().toString();
        val resource = resourceCreator.create(location(id));
        try (OutputStream outputStream = resource.getOutputStream()) {
            
        }
        updater.update(data, "id", id);
        return id;
    }

    @Override
    public String read(@NonNull String id) throws UnknownFileException, IOException {
        Resource file = resourceLoader.getResource(location(id));
        if (file.exists()) try (InputStream inputStream = file.getInputStream()) {
            return StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"));
        }
        else {
            throw new UnknownFileException();
        }
    }

    @Override
    public void update(String id, String data) {

    }

    @Override
    public void delete(String id) {

    }

    private String location(String id) {
        return format("%s/%s.json", datastore, id);
    }
}
