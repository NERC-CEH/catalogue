package uk.ac.ceh.gateway.catalogue.method;

import lombok.NonNull;
import lombok.val;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/method.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class MethodRecord extends AbstractMetadataDocument{

    private List<ResponsibleParty> contacts;
    private List<OnlineResource> onlineResources;
    private List<AdditionalInfo> additionalInfo;

    @Data
    public static class AdditionalInfo {
        private String
            key,
            value;
    }
}


