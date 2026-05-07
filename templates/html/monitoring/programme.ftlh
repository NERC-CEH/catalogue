<#import "monitoringBase.ftlh" as m>
<@m.base>

  <#assign CombinedGeometriesOutput>
    <@m.programmeCombinedGeometries "https://digital.ceh.ac.uk/ontology/doo/utilises" />
  </#assign>

  <#if CombinedGeometriesOutput?has_content>
    <div class="displayFeatures">
      <div class="featureMap">${CombinedGeometriesOutput}</div>
      <div class="featureList">
        <@m.relationships "https://digital.ceh.ac.uk/ontology/doo/utilises" "Uses"  />
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
    <#if !CombinedGeometriesOutput?has_content><@m.relationships "https://digital.ceh.ac.uk/ontology/doo/utilises" "Uses" /></#if>

    <@m.relationships  "http://purl.org/dc/terms/replaces" "Supersedes" />
    <@m.inverseRelationships  "http://purl.org/dc/terms/replaces" "Superseded by"/>

    <@m.relationships "https://digital.ceh.ac.uk/ontology/doo/triggers" "Activities set up for this programme" />

    <@m.inverseRelationships "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme" "Parent programmes" />
    <@m.relationships "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme" "Child programmes" />

    <@m.symmetricRelationships "http://purl.org/dc/terms/relation" "Related" />
  </div>
</@m.base>
