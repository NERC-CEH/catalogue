package uk.ac.ceh.gateway.catalogue.datacite;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Value;
import org.apache.commons.codec.binary.Base64;

@Value
@JsonRootName("data")
public class DataciteRequest {
    public DataciteRequest(String id, String request, String url) {
        this.id = id;
        this.type = "dois";
        this.atttributes = new Attributes(request, url);
    }

    String id; // this is the DOI
    String type; // fixed as 'dois'
    Attributes atttributes;

    @Value
    public static class Attributes {
        public Attributes(String xml, String url) {
            byte[] bytesEncoded = Base64.encodeBase64(xml.getBytes());
            this.xml = new String(bytesEncoded);
            this.event = "publish";
            this.url = url;
        }

        String event; // fixed as "publish" String doi;
        String url; //fixed as "https://schema.datacite.org/meta/kernel-4.0/index.html"
        String xml; // base64 encoded xml from the Freemarker template
    }
}