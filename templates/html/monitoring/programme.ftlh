<#import "monitoringBase.ftlh" as m>
<@m.base>

  <#assign CombinedGeometriesOutput>
    <@m.programmeCombinedGeometries combinedGeometry />
  </#assign>

  <#if CombinedGeometriesOutput?has_content>
    <div class="displayFeatures">
      <div class="featureMap">${CombinedGeometriesOutput}</div>
      <div class="featureList">
        <@m.relationships uses "Uses"  />
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
    <#if !CombinedGeometriesOutput?has_content><@m.relationships uses "Uses" /></#if>

    <@m.relationships  supersedes "Supersedes" />
    <@m.inverseRelationships  supersededBy "Superseded by"/>

    <@m.relationships activities "Activities set up for this programme" />

    <@m.inverseRelationships parentProgramme "Parent programmes" />
    <@m.relationships childProgramme "Child programmes" />

    <@m.symmetricRelationships related "Related" />
  </div>
</@m.base>
