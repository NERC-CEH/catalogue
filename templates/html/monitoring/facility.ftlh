<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign relationshipCombinedGeometriesOutput>
    <@m.relationshipCombinedGeometriesWithOwner combinedGeometry />
  </#assign>

  <#if locationConfidential>
    <div class="locationConfidential">The location of this facility is confidential, so the position shown on the map is just illustrative</div>
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
    <@m.relationships belongsToNetwork "Belongs to these networks"/>

    <@m.inverseRelationships usedBy "Used by"  />
    <@m.inverseRelationships utilisedBy "Used by"  />

    <@m.relationships supersedes "Supersedes" />
    <@m.inverseRelationships supersededBy "Superseded by"  />

    <@m.relationships childFacility "Child facilities" />
    <@m.inverseRelationships parentFacility "Parent facilities"  />

    <@m.symmetricRelationships related "Related"/>
  </#compress></div>
</@m.base>
