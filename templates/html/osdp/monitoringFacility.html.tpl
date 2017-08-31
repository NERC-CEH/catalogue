<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.base>
  <#if facilityType?? && facilityType?has_content>
    <@b.key "Type" "Type of Monitoring Facility">${facilityType}</@b.key>
  </#if>
  <#if boundingBox?? && boundingBox?has_content>
    <@b.key "Bounding Box" "Bounding Box of Monitoring Facility"><@o.boundingBox boundingBox /></@b.key>
  </#if>
  <#if observationCapabilities?? && observationCapabilities?has_content>
    <@b.key "Observation Capabilities" "Observation Capabilities of Monitoring Facility">
    <#list observationCapabilities as observationCapability>
      <@o.observationCapability observationCapability />
    </#list>
    </@b.key>
  </#if>
  <@o.relationships "Part Of" "Part of another Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
  <@o.inverseRelationships "Used" "Monitoring Activities using this Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
</@o.base>