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

  <div class="relationships">
    <@m.inverseRelationships "http://onto.nerc.ac.uk/CEHMD/rels/uses" "Used by" />
    <@m.inverseRelationships "http://onto.nerc.ac.uk/CEHMD/rels/uses" "Used by" />

    <@m.relationships "http://onto.ceh.ac.uk/EF#belongsTo" "Belongs to these networks"/>

    <@m.relationships  "http://onto.ceh.ac.uk/EF#supersedes" "Superseded by"/>
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#supersedes" "Superseded by"/>

    <@m.symmetricRelationships "http://onto.ceh.ac.uk/EF#associatedWith" "Related"/>
  </div>
</@m.base>
