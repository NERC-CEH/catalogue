<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign relationshipCombinedGeometriesOutput>
    <@m.relationshipCombinedGeometriesWithOwner "http://www.w3.org/2004/02/skos/core#narrower" locationConfidential />
  </#assign>


  <#if locationConfidential>
    <div class="locationConfidential">The location of this facility is confidential, so the position shown on the map is just illustrative</div>
  </#if>

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

  <div class="relationships"><#compress>
    <@m.relationships "http://purl.org/dc/terms/isPartOf" "Belongs to these networks"/>

    <@m.inverseRelationships "https://digital.ceh.ac.uk/ontology/doo/uses" "Used by"  />
    <@m.inverseRelationships "https://digital.ceh.ac.uk/ontology/doo/utilises" "Used by"  />

    <@m.relationships "http://purl.org/dc/terms/replaces" "Supersedes" />
    <@m.inverseRelationships "http://purl.org/dc/terms/replaces" "Superseded by"  />

    <@m.relationships "http://www.w3.org/2004/02/skos/core#narrower" "Child facilities" />
    <@m.inverseRelationships "http://www.w3.org/2004/02/skos/core#narrower" "Parent facilities"  />

    <@m.symmetricRelationships "http://purl.org/dc/terms/relation" "Related"/>
  </#compress></div>
</@m.base>
