package uk.ac.ceh.gateway.catalogue.ef;

import javax.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;

@Data
@Accessors(chain = true)
@XmlType(propOrder = {
    "fundingCategory",
    "futureFundingStatus",
    "risksToFunding",
    "fundingNotes"
})
public class Funding {
    private String futureFundingStatus, risksToFunding, fundingNotes;
    private List<Link> fundingCategory;
}
