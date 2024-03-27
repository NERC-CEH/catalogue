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

  <@m.inverseRelationships "Contains" "Contains" "http://purl.org/voc/ef#belongsTo" />
  <@m.inverseRelationships "Used by" "Used by" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
</@m.base>
