<#import "monitoringBase.ftlh" as m>
<@m.base>

  <#assign CombinedGeometriesOutput>
    <@m.programmeCombinedGeometries relCombinedGeometry />
  </#assign>

  <#if CombinedGeometriesOutput?has_content>
    <div class="displayFeatures">
      <div class="featureMap">${CombinedGeometriesOutput}</div>
      <div class="featureList">
        <@m.relationships relUses "Uses"  />
      </div>
    </div>
  <#else>
    <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <@m.map boundingBox.bounds />
    </@m.keyValue>
    </#if>
  </#if>


  <div class="relationships">
    <#if !CombinedGeometriesOutput?has_content><@m.relationships relUses "Uses" /></#if>

    <@m.relationships  relSupersedes "Supersedes" />
    <@m.inverseRelationships  relSupersededBy "Superseded by"/>

    <@m.relationships relActivities "Activities set up for this programme" />

    <@m.inverseRelationships relParentProgramme "Parent programmes" />
    <@m.relationships relChildProgramme "Child programmes" />

    <@m.symmetricRelationships relRelated "Related" />
  </div>
</@m.base>
