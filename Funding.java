package uk.ac.ceh.ukeof.model.simple;

import javax.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.experimental.Accessors;

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
    private Link fundingCategory;
}