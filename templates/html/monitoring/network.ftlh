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

  <div class="relationships">

    <@m.inverseRelationships relUsedBy "Used by"  />
    <@m.inverseRelationships relUtilisedBy "Used by"  />

    <@m.relationships relSupersedes "Supersedes" />
    <@m.inverseRelationships relSupersededBy "Superseded by"  />

    <@m.relationships relChildNetwork "Child networks" />
    <@m.inverseRelationships relParentNetwork "Parent networks"  />

    <@m.symmetricRelationships relRelated "Related"/>
  </div>

</@m.base>
