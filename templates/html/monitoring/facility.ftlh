<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#if facilityType?? && facilityType?has_content>
    <@m.keyValue "Type" "type of monitoring facility">${facilityType.value}</@m.keyValue>
  </#if>

  <#if geometry?? && geometry?has_content>
    <div>
    <#if geometry.geometryString?? && geometry.geometryString?has_content>
      <@m.keyValue "Location">
        <@m.map geometry.geometryString />
      </@m.keyValue>

      <#if geometryRepresentative>
        Geometry is representative
      </#if>
    </#if>
    </div>
  </#if>

  <#if mobile>
    <@m.keyValue "Mobile?">Yes</@m.keyValue>
  </#if>

  <div class="relationships">
    <@m.relationships "http://onto.ceh.ac.uk/EF#belongsTo" "Belongs to these networks"/>

    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#uses" "Used by"  />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#utilises" "Used by"  />

    <@m.relationships "http://onto.ceh.ac.uk/EF#supersedes" "Supersedes" />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#supersedes" "Superseded by"  />

    <@m.relationships "http://onto.ceh.ac.uk/EF#narrower" "Child facilities" />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#narrower" "Parent facilities"  />

    <@m.symmetricRelationships "http://onto.ceh.ac.uk/EF#associatedWith" "Related"/>
  </div>

</@m.base>
