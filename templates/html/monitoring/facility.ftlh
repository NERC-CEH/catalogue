<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign relationshipCombinedGeometriesOutput>
    <@m.relationshipCombinedGeometriesWithOwner relCombinedGeometry />
  </#assign>

  <#include "_locationConfidential.ftlh">

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

  <#include "_facilityRelationships.ftlh">
</@m.base>
