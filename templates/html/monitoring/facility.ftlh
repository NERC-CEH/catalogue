<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#if facilityType?? && facilityType?has_content>
    <@m.keyValue "Type" "type of monitoring facility">${facilityType.value}</@m.keyValue>
  </#if>

  <#if geometry.geometryString?? && geometry.geometryString?has_content>
  <div>
    <@m.keyValue "Location">
      <@m.map geometry.geometryString />
    </@m.keyValue>

    <#if mobile>
       <@m.keyValue "Mobile?">Yes</@m.keyValue>
    </#if>

    <#if geometryRepresentative>
      Geometry is representative
    </#if>
  </div>
  </#if>

    <@m.inverseRelationships "Uses" "Used by these activities" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
    <@m.relationships "Belongs To" "Belongs to these networks" "http://purl.org/voc/ef#belongsTo" />
    <@m.relationships "Supersedes" "Supersedes" "http://purl.org/voc/ef#supersedes" />
    <@m.inverseRelationships "Superseded by" "Superseded by" "http://purl.org/voc/ef#supersedes" />
</@m.base>
