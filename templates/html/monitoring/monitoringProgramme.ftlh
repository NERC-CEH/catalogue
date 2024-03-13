<#import "monitoringBase.ftlh" as o>
<#import "../blocks.ftlh" as b>

<@o.base>
    <#if temporalExtent?? && temporalExtent?has_content>
      <@b.key "Temporal Extent" "Temporal extent of Monitoring Programme">
          <@o.temporalExt temporalExtent />
      </@b.key>
    </#if>
    <#if boundingBox?? && boundingBox?has_content>
        <@b.key "Bounding Box" "Bounding Box of Monitoring Programme"><@o.boundingBox boundingBox /></@b.key>
    </#if>
    <@o.inverseRelationships "Triggers" "Monitoring Activities triggered by this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/setupFor" />
    <@o.relationships "Associated With" "Projects associated with this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/associatedWith" />
    <@o.relationships "Owns" "Research Artifacts owned by this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/owns" />
    <@o.inverseRelationships "Associated With" "Agents associated with this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/associatedWith" />
</@o.base>
