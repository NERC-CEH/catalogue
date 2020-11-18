package uk.ac.ceh.gateway.catalogue.upload.simple;

import com.google.common.net.UrlEscapers;
import lombok.Value;

@Value
public
class FileInfo {
    String name;
    String urlEncodedName;

    public FileInfo(String filename) {
        this.name = filename;
        this.urlEncodedName = UrlEscapers.urlPathSegmentEscaper().escape(filename);
    }
}
