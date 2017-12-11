package uk.ac.ceh.gateway.catalogue.sa;

import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/sample_archive/sample_archive.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class SampleArchive extends AbstractMetadataDocument {
  private String lineage, language, availability, accessRestrictions, storage, healthSafety;
  private List<Keyword> specimenTypes, topicCategories, keywords;
  private TimePeriod temporalExtent;
  private List<BoundingBox> boundingBoxes;
  private List<String> archiveLocations;
  private List<ResponsibleParty> contacts;}