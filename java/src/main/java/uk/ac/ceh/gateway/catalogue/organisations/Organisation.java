package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

/**
 * An organisation from the ROR data dump, as the editor's organisation look-up offers it. Beside the
 * name, where it is, what kind it is and its website tell same-named organisations apart (dri-one #417):
 * there are two "York University"s, in Nebraska and in Toronto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {
    @Field private String id;
    @Field private String name;
    @Field private List<String> acronyms;
    @Field private List<String> aliases;
    @Field private String city;
    /** The country subdivision: a state, province or, in the UK, England, Scotland, Wales or Northern Ireland */
    @Field private String region;
    @Field private String country;
    /** ISO 3166-1 alpha-2, e.g. GB */
    @Field private String countryCode;
    /** ROR organisation types: education, funder, healthcare, … */
    @Field private List<String> types;
    @Field private String website;
}
