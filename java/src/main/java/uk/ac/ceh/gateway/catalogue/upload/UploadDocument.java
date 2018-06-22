package uk.ac.ceh.gateway.catalogue.upload;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.springframework.http.MediaType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/upload/upload-document.ftl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
public class UploadDocument extends AbstractMetadataDocument {
    private final String parentId;
    private final Map<String, UploadFiles> uploadFiles;

    public String getCsv(String ...names) {
        List<String> csv = Lists.newArrayList();
        csv.add(String.format("Filename,Hash"));
        for (val name : names) {
            val uploadFiles = this.uploadFiles.get(name);
            appendToCsv(uploadFiles.getDocuments(), csv);
            appendToCsv(uploadFiles.getInvalid(), csv);
        }
        return StringUtils.join(csv, "\n");
    }

    private void appendToCsv(Map<String, UploadFile> uploadFiles, List<String> csv) {
        
        for (val uploadFile : uploadFiles.values()) {
            csv.add(String.format("%s,%s", uploadFile.getName(), uploadFile.getHash()));
        }
    }

}