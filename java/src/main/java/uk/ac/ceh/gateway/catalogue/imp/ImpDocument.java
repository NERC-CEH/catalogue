package uk.ac.ceh.gateway.catalogue.imp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.ef.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonTypeInfo(use=Id.NAME, include=As.EXISTING_PROPERTY, property="type", visible=true)
@JsonSubTypes({
    @Type(name="model",            value = Model.class),
    @Type(name="modelApplication", value = ModelApplication.class),
    @Type(name="caseStudy",        value = CaseStudy.class)
})
public class ImpDocument extends AbstractMetadataDocument {
    private ResponsibleParty contact;

}
