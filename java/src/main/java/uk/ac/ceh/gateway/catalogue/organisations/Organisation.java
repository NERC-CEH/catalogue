package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

@Data
@AllArgsConstructor
public class Organisation {
    @Field private String id;
    @Field private String name;
    @Field private List<String> acronyms;
    @Field private List<String> aliases;

    public Organisation() {}
}
