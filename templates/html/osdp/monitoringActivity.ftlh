<#import "osdp.ftlh" as o>
<#import "../blocks.ftlh" as b>

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
  <#if boundingBox?? && boundingBox?has_content>
    <@b.key "Bounding Box" "Bounding Box of Monitoring Activity"><@o.boundingBox boundingBox /></@b.key>
  </#if>
  <@o.inverseRelationships "Associated With" "Agents associated with this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/associatedWith" />
  <@o.relationships "Produces" "Research Artifacts produced by this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/produces" />
  <@o.relationships "Uses" "Monitoring Facilities used by this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@o.inverseRelationships "Used By" "Used by this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@o.relationships "Setup for" "This activity was set up for these Monitoring Programmes" "http://onto.nerc.ac.uk/CEHMD/rels/setupFor" />
</@o.base>
