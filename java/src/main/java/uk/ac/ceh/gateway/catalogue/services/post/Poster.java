package uk.ac.ceh.gateway.catalogue.services.post;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.Charset;

public class Poster {
    public Poster(){}
    public void doPost() {
        System.out.println("About to post big ttl");
        RestTemplate restTemplate = new RestTemplate();
        String serverUrl = "https://eidc-fuseki.staging.ceh.ac.uk/ds?graph=http://example.com/coopers_graph/";

        String username = "user";
        String password = "password";
        HttpHeaders headers = createHeaders(username, password);
        headers.add(HttpHeaders.CONTENT_TYPE, "text/turtle");

        HttpEntity<String> request = new HttpEntity<>(getBody(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, request, String.class);
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());

    }
    private HttpHeaders createHeaders (String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};

    }

    private String getBody(){
        return "@prefix skos: <http://www.w3.org/2004/02/skos/core#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix dct: <http://purl.org/dc/terms/> .\n" +
                "@prefix dcat: <http://www.w3.org/ns/dcat#> .\n" +
                "@prefix dcmitype: <http://purl.org/dc/dcmitype/> .\n" +
                "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n" +
                "@prefix geo: <http://www.opengis.net/ont/geosparql#> .\n" +
                "@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .\n" +
                "@prefix eidc: <https://catalogue.ceh.ac.uk/id/> .\n" +
                "<http://example.com/coopers_graph/> a dcat:Catalog ;\n" +
                "    dct:title \"EIDC digital asset register\"@en ;\n" +
                "    foaf:homepage <https://catalogue.ceh.ac.uk/eidc/documents> ;\n" +
                "    dcat:resource eidc:mapserver-raster-byte ,eidc:ff55462e-38a4-4f30-b562-f82ff263d9c3 ,eidc:abcdef12-3456-1234-0987-6876abcd1234 ,eidc:1e7d5e08-9e24-471b-ae37-49b477f695e3 ,eidc:c88921ba-f871-44c3-9339-51c5bee4024a ,eidc:2d023ce9-6dbe-4b4f-a0cd-34768e1455ae ,eidc:mapserver-all-features ,eidc:mapserver-raster ,eidc:bb2d7874-7bf4-44de-aa43-348bd684a2fe ,eidc:mapserver-shapefile ,eidc:mapserver-file-gdb ,eidc:mapserver-multiple-projections ,eidc:986d3df3-d9bf-42eb-8e18-850b8d54f37b  ;\n" +
                ".\n" +
                "\n" +
                "<https://ror.org/04xw4m193> a vcard:Organization ;\n" +
                "    vcard:fn \"NERC EDS Environmental Information Data Centre\" ;\n" +
                ".\n" +
                "<https://ror.org/00pggkr55> a vcard:Organization ;\n" +
                "    vcard:fn \"UK Centre for Ecology & Hydrology\" ;\n" +
                ".\n" +
                "<http://localhost:8080/id/mapserver-raster-byte>\n" +
                "dct:title \"Cooperman Demo of MapServer Raster (byte) Service\" ;\n" +
                "dct:description \"This is a metadata record which demonstrates a mapserver powered view service\" ;\n" +
                "dct:spatial \"POLYGON((-0.53 51.28, -0.53 51.72, 0.33 51.72, 0.33 51.28, -0.53 51.28))\"^^geo:wktLiteral ;\n" +
                "dct:language \"eng\" ;\n" +
                "dct:subject\n" +
                "\"London\",\"Population\",\"Tea\",\"2016\" ;\n" +
                "a dcmitype:Service ;\n" +
                ".";
    }

}
