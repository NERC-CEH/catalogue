<#import "base.ftlh" as o>
<#import "../blocks.ftlh" as b>

<@o.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@o.inverseRelationshipCombinedGeometries "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
  </#assign>
  <#if inverseRelationshipCombinedGeometriesOutput?has_content>
    <@b.key "Location" "Monitoring Network">
      ${inverseRelationshipCombinedGeometriesOutput}
    </@b.key>
  </#if>

  <@o.inverseRelationships "Contains" "Contains Monitoring Facilities" "http://purl.org/voc/ef#belongsTo" />
</@o.base>
