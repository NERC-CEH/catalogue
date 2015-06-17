package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 *
 * @author cjohn
 */
@Data
public class DocumentUpload {
    private CommonsMultipartFile file;
    private String comment;
}