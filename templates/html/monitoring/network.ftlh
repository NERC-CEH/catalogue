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

    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#uses" "Used by"  />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#utilises" "Used by"  />

    <@m.relationships "http://purl.org/dc/terms/replaces" "Replaces" />
    <@m.inverseRelationships "http://purl.org/dc/terms/replaces" "Replaced by"  />

    <@m.relationships "http://www.w3.org/2004/02/skos/core#narrower" "Child networks" />
    <@m.inverseRelationships "http://www.w3.org/2004/02/skos/core#narrower" "Parent networks"  />

    <@m.symmetricRelationships "http://purl.org/dc/terms/related" "Related"/>
  </div>

</@m.base>
