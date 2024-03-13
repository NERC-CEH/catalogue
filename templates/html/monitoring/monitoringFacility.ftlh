<#import "monitoringBase.ftlh" as o>
<#import "../blocks.ftlh" as b>

<@o.base>
  <#if facilityType?? && facilityType?has_content>
    <@b.key "Type" "Type of Monitoring Facility">${facilityType}</@b.key>
  </#if>
   <#if temporalExtent?? && temporalExtent?has_content>
    <@b.key "Temporal Extent" "Temporal extent of Monitoring Facility">
        <@o.temporalExt temporalExtent />
    </@b.key>
  </#if>
  <#if boundingBox?? && boundingBox?has_content>
    <@b.key "Bounding Box" "Bounding Box of Monitoring Facility"><@o.boundingBox boundingBox /></@b.key>
  </#if>
  <#if geometry?? && geometry?has_content>
    <@b.key "Location" "Location of monitoring facility"><@o.geometry geometry.geometryString /></@b.key>
  </#if>

  <@o.relationships "Belongs To" "Belongs to Monitoring Network" "http://purl.org/voc/ef#belongsTo" />
</@o.base>
