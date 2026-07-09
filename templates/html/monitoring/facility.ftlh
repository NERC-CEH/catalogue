<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign relationshipCombinedGeometriesOutput>
    <@m.relationshipCombinedGeometriesWithOwner relCombinedGeometry />
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
    <@m.relationships relBelongsToNetwork "Belongs to these networks"/>

    <@m.inverseRelationships relUsedBy "Used by"  />
    <@m.inverseRelationships relUtilisedBy "Used by"  />

    <@m.relationships relSupersedes "Supersedes" />
    <@m.inverseRelationships relSupersededBy "Superseded by"  />

    <@m.relationships relChildFacility "Child facilities" />
    <@m.inverseRelationships relParentFacility "Parent facilities"  />

    <@m.symmetricRelationships relRelated "Related"/>
  </#compress></div>
</@m.base>
