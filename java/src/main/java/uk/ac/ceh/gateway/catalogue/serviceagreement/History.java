package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ConvertUsing({
        @Template(called = "html/service-agreement-history.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
public class History {
    private String historyOf;
    private List<Revision> revisions;

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Revision {
        private String id;
        private String href;
    }
}
