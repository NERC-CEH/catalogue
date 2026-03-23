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
    <#if !CombinedGeometriesOutput?has_content><@m.relationships "https://digital.ceh.ac.uk/ontology/doo/utilises" "Uses"  /></#if>

    <@m.relationships  "http://purl.org/dc/terms/replaces" "Replaces" />
    <@m.inverseRelationships  "http://purl.org/dc/terms/replaces" "Replaced by"/>

    <@m.relationships "https://digital.ceh.ac.uk/ontology/doo/triggers" "Activities set up for this programme"  />

    <@m.inverseRelationships "http://www.w3.org/2004/02/skos/core#narrower" "Parent programmes"  />
    <@m.relationships "http://www.w3.org/2004/02/skos/core#narrower" "Child programmes"  />

    <@m.symmetricRelationships "http://purl.org/dc/terms/related" "Related"/>
  </div>
</@m.base>
