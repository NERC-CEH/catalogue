<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@m.inverseRelationshipCombinedGeometries "http://purl.org/voc/ef#belongsTo" />
  </#assign>

  <div class="networkMap">
    <#if inverseRelationshipCombinedGeometriesOutput?has_content>
     <div>${inverseRelationshipCombinedGeometriesOutput}</div>
    </#if>
    <div class="facilitiesList">
      <@m.inverseRelationships2 "Contains" "http://purl.org/voc/ef#belongsTo" />
    </div>
  </div>

  <@m.inverseRelationships "Used by" "Used by" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />

</@m.base>
