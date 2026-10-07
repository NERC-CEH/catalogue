<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@m.inverseRelationshipCombinedGeometries relCombinedGeometry />
  </#assign>

  <div class="displayFeatures">
    <#if inverseRelationshipCombinedGeometriesOutput?has_content>
     <div class="featureMap">${inverseRelationshipCombinedGeometriesOutput}</div>
    </#if>
    <div class="featureList">
      <@m.inverseRelationships relFeatureList />
    </div>
  </div>

  <#include "_networkRelationships.ftlh">

</@m.base>
