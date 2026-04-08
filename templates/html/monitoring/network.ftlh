<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@m.inverseRelationshipCombinedGeometries "http://purl.org/dc/terms/isPartOf" />
  </#assign>

  <div class="displayFeatures">
    <#if inverseRelationshipCombinedGeometriesOutput?has_content>
     <div class="featureMap">${inverseRelationshipCombinedGeometriesOutput}</div>
    </#if>
    <div class="featureList">
      <@m.inverseRelationships "http://purl.org/dc/terms/isPartOf" />
    </div>
  </div>

  <div class="relationships">

    <@m.inverseRelationships "https://digital.ceh.ac.uk/ontology/doo/uses" "Used by"  />
    <@m.inverseRelationships "https://digital.ceh.ac.uk/ontology/doo/utilises" "Used by"  />

    <@m.relationships "http://purl.org/dc/terms/replaces" "Supersedes" />
    <@m.inverseRelationships "http://purl.org/dc/terms/replaces" "Superseded by"  />

    <@m.relationships "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork" "Child networks" />
    <@m.inverseRelationships "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork" "Parent networks"  />

    <@m.symmetricRelationships "http://purl.org/dc/terms/relation" "Related"/>
  </div>

</@m.base>
