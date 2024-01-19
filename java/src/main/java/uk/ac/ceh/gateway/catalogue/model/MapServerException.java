package uk.ac.ceh.gateway.catalogue.model;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * If an issue occurs when talking to the mapserver (for getfeatureinfo requests)
 * we will want to proxy that error back to the end user.
 */
public class MapServerException extends RuntimeException {
    static final long serialVersionUID = 1L;
    @Getter private final MediaType contentType;
    @Getter private final HttpStatus statusCode;

    public MapServerException(String mess, HttpStatus statusCode, MediaType contentType) {
        super(mess);
        this.statusCode = statusCode;
        this.contentType = contentType;
    }

    /**
     * Get the MapServerException in a form which can be returned to the end user
     * @return ResponseEntity containing a String body
     */
    public ResponseEntity<String> asResponseEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return new ResponseEntity<>(getMessage(), headers, statusCode);
    }
}
