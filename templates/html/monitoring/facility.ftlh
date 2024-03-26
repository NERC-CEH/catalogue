<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#if facilityType?? && facilityType?has_content>
    <@m.keyValue "Type" "type of monitoring facility">${facilityType.value}</@m.keyValue>
  </#if>

  <#if geometry?? && geometry?has_content>
  <div>
    <@m.keyValue "Location">
      <@m.map geometry.geometryString />
    </@m.keyValue>

    <#if mobile>
      MOBILE
    </#if>

    <#if geometryRepresentative>
      Geometry is representative
    </#if>
  </div>
  </#if>

  <@m.relationships "Belongs To" "Belongs to Monitoring Network" "http://purl.org/voc/ef#belongsTo" />
</@m.base>
