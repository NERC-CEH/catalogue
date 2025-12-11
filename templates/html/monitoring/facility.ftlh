<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign relationshipCombinedGeometriesOutput>
    <@m.relationshipCombinedGeometriesWithOwner "http://onto.ceh.ac.uk/EF#narrower" locationConfidential />
  </#assign>


  <#if locationConfidential>
    <b>The location of this facility is confidential, so the position shown on the map is just representative</b>
  </#if>

  <#if relationshipCombinedGeometriesOutput?has_content>
    <div class="displayFeatures">
        <div class="featureMap">${relationshipCombinedGeometriesOutput}</div>
    </div>
  </#if>

  <#if mobile>
    <@m.keyValue "Mobile?">Yes</@m.keyValue>
  </#if>

  <#if facilityType?? && facilityType?has_content>
    <@m.keyValue "Facility type" "type of monitoring facility">${facilityType.value}</@m.keyValue>
  </#if>

  <div class="relationships"><#compress>
    <@m.relationships "http://onto.ceh.ac.uk/EF#belongsTo" "Belongs to these networks"/>

    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#uses" "Used by"  />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#utilises" "Used by"  />

    <@m.relationships "http://onto.ceh.ac.uk/EF#supersedes" "Supersedes" />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#supersedes" "Superseded by"  />

    <@m.relationships "http://onto.ceh.ac.uk/EF#narrower" "Child facilities" />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#narrower" "Parent facilities"  />

    <@m.symmetricRelationships "http://onto.ceh.ac.uk/EF#associatedWith" "Related"/>
  </#compress></div>

</@m.base>
