<#import "base.ftlh" as m>
<#import "../blocks.ftlh" as b>

<@m.base>
  <#if parametersMeasured?? && parametersMeasured?has_content>
    <@b.key "Parameters Measured" "Parameters measured in Monitoring Activity">
      <#list parametersMeasured as parameter>
        <@m.parametersMeasured parameter />
      </#list>
    </@b.key>
  </#if>
  <#if temporalExtent?? && temporalExtent?has_content>
    <@b.key "Temporal Extent" "Temporal extent of Monitoring Activity">
        <@m.temporalExt temporalExtent />
    </@b.key>
  </#if>
  <#if boundingBox?? && boundingBox?has_content>
    <@b.key "Bounding Box" "Bounding Box of Monitoring Activity"><@m.boundingBox boundingBox /></@b.key>
  </#if>
  <@m.inverseRelationships "Associated With" "Agents associated with this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/associatedWith" />
  <@m.relationships "Produces" "Research Artifacts produced by this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/produces" />
  <@m.relationships "Uses" "Monitoring Facilities used by this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.inverseRelationships "Used By" "Used by this artifact" "http://ontm.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.relationships "Setup for" "This activity was set up for these Monitoring Programmes" "http://ontm.nerc.ac.uk/CEHMD/rels/setupFor" />
</@m.base>



