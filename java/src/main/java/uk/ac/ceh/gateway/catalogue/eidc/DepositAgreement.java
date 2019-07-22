package uk.ac.ceh.gateway.catalogue.eidc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.eidc.Author;
import uk.ac.ceh.gateway.catalogue.eidc.File;

import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
  @Template(called="html/eidc/deposit_agreement.ftl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class DepositAgreement extends AbstractMetadataDocument{
  private String datasetMetadataID, jiraReference, dataCategory, recordIdentifier;
  private List<ResponsibleParty> depositor, dco;
  private List<File> dataFiles, supportingFiles;
  private String dataFilesOther, policies, dataRetention, onlineAccessRequirements, otherServices, supersededData, supersededReason, miscellaneous, availability, embargoDate;
  private List<Author> authors;
  private List<String> dataLocations;
  private List<ResourceConstraint> licence, useConstraints;
  private boolean agreed;
  private String agreedBy, agreedDate;
}
