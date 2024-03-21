<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@m.inverseRelationshipCombinedGeometries "http://purl.org/voc/ef#belongsTo" />
  </#assign>
  <#if inverseRelationshipCombinedGeometriesOutput?has_content>
    <@m.keyValue "Location" "Monitoring Network">
      ${inverseRelationshipCombinedGeometriesOutput}
    </@m.keyValue>
  </#if>

  <@m.inverseRelationships "Contains" "Contains Monitoring Facilities" "http://purl.org/voc/ef#belongsTo" />
</@m.base>
