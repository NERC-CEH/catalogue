<#import "monitoringBase.ftlh" as m>

<@m.base>
    <#if temporalExtent?? && temporalExtent?has_content>
      <@m.keyValue "Temporal Extent" "Temporal extent of Monitoring Programme">
          <@m.temporalExt temporalExtent />
      </@m.keyValue>
    </#if>
    <#if boundingBox?? && boundingBox?has_content>
        <@m.keyValue "Bounding Box" "Bounding Box of Monitoring Programme"><@m.boundingBox boundingBox /></@m.keyValue>
    </#if>
    <@m.inverseRelationships "Triggers" "Monitoring Activities triggered by this Monitoring Programme" "http://ontm.nerc.ac.uk/CEHMD/rels/setupFor" />
    <@m.relationships "Associated With" "Projects associated with this Monitoring Programme" "http://ontm.nerc.ac.uk/CEHMD/rels/associatedWith" />
    <@m.relationships "Owns" "Research Artifacts owned by this Monitoring Programme" "http://ontm.nerc.ac.uk/CEHMD/rels/owns" />
    <@m.inverseRelationships "Associated With" "Agents associated with this Monitoring Programme" "http://ontm.nerc.ac.uk/CEHMD/rels/associatedWith" />
</@m.base>
