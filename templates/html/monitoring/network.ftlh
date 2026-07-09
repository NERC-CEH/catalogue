<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@m.inverseRelationshipCombinedGeometries combinedGeometry />
  </#assign>

  <div class="displayFeatures">
    <#if inverseRelationshipCombinedGeometriesOutput?has_content>
     <div class="featureMap">${inverseRelationshipCombinedGeometriesOutput}</div>
    </#if>
    <div class="featureList">
      <@m.inverseRelationships featureList />
    </div>
  </div>

  <div class="relationships">

    <@m.inverseRelationships usedBy "Used by"  />
    <@m.inverseRelationships utilisedBy "Used by"  />

    <@m.relationships supersedes "Supersedes" />
    <@m.inverseRelationships supersededBy "Superseded by"  />

    <@m.relationships childNetwork "Child networks" />
    <@m.inverseRelationships parentNetwork "Parent networks"  />

    <@m.symmetricRelationships related "Related"/>
  </div>

</@m.base>
