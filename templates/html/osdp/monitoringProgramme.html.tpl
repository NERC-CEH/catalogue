<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.base>
    <#if temporalExtent?? && temporalExtent?has_content>
      <@b.key "Temporal Extent" "Temporal extent of Monitoring Programme">
          <@o.temporalExt temporalExtent /> 
      </@b.key>
    </#if>
    <#if boundingBox?? && boundingBox?has_content>
        <@b.key "Bounding Box" "Bounding Box of Monitoring Programme"><@o.boundingBox boundingBox /></@b.key>
    </#if>
    <@o.relationships "Setup For" "Monitoring Activities setup for this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/setupFor" />
    <@o.relationships "Associated With" "Projects associated with this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/associatedWith" />
    <@o.relationships "Owns" "Research Artifacts owned by this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/owns" />
</@o.base>