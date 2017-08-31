<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.base>
  <#if parametersMeasured?? && parametersMeasured?has_content>
    <@b.key "Parameters Measured" "Parameters measured in Monitoring Activity">
      <#list parametersMeasured as parameter>
        <@o.parametersMeasured parameter /> 
      </#list>
    </@b.key>
  </#if>
  <#if temporalExtent?? && temporalExtent?has_content>
    <@b.key "Temporal Extent" "Temporal extent of Monitoring Activity">
        <@o.temporalExt temporalExtent /> 
    </@b.key>
  </#if>
  <@o.relationships "Produces" "Research Artifacts produced by this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/produces" />
  <@o.relationships "Uses" "Monitoring Facilities used by this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@o.inverseRelationships "Used By" "Used by this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@o.inverseRelationships "Setup By" "Setup by this Monitoring Programme" "http://onto.nerc.ac.uk/CEHMD/rels/setupFor" />
</@o.base>