package uk.ac.ceh.gateway.catalogue.datacite;

import lombok.Value;
import org.apache.commons.codec.binary.Base64;

@Value
public class DataciteRequest {
    Data data;

    public DataciteRequest(String doi, String request, String url) {
        this.data = new Data(doi, new Attributes(doi, request, url));
    }

    @Value
    public static class Data {
        String id; // this is the DOI
        String type = "dois";
        Attributes attributes;
    }

    @Value
    public static class Attributes {
        public Attributes(String doi, String xml, String url) {
            this.doi = doi;
            this.xml = new String(Base64.encodeBase64(xml.getBytes()));
            this.url = url;
        }
        String doi;
        String event = "publish";
        String url; // url of DOI landing page
        String xml; // base64 encoded Datacite xml
    }
}
