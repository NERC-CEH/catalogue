package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.apache.solr.client.solrj.beans.Field;

@Value
public class ServiceAgreementSolrIndex {
    @Field String dataIdentifier;
    @Field String depositorName;
    @Field String depositReference;
    @Field String eidcName;
    @Field String title;

    @JsonCreator
    public ServiceAgreementSolrIndex(
        @JsonProperty("dataIdentifier") String dataIdentifier,
        @JsonProperty("depositorName") String depositorName,
        @JsonProperty("depositReference") String depositReference,
        @JsonProperty("eidcName") String eidcName,
        @JsonProperty("title") String title
    ) {
        this.dataIdentifier = dataIdentifier;
        this.depositorName = depositorName;
        this.depositReference = depositReference;
        this.eidcName = eidcName;
        this.title = title;
    }
}
