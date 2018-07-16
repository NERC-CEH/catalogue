package uk.ac.ceh.gateway.catalogue.file;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;

import java.io.IOException;
import java.net.URI;

import static java.lang.String.format;


@RestController
@AllArgsConstructor
@Secured(DocumentController.MAINTENANCE_ROLE)
@SuppressWarnings("unused")
public class FileController {
    private final FileRepository fileRepository;

    @PostMapping(value = "file", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> create(@RequestBody String data) {
        val id = fileRepository.create(data);
        return ResponseEntity.created(URI.create(format("/file/%s", id))).build();
    }

    @SneakyThrows
    @GetMapping(value = "file/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HttpEntity<String> read(@PathVariable("id") String id) {
        return ResponseEntity.ok(fileRepository.read(id));
    }

    @PutMapping(value = "file/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HttpEntity update(@PathVariable("id") String id, @RequestBody String data) {
        fileRepository.update(id, data);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("file/{id}")
    public HttpEntity delete(@PathVariable("id") String id) {
        fileRepository.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UnknownFileException.class)
    public void handleUnknownFileException() {}

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public void handleIOException() {}

}
