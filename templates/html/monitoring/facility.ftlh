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
    <@m.relationships "http://purl.org/dc/terms/isPartOf" "Is part of these networks"/>

    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#uses" "Used by"  />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#utilises" "Used by"  />

    <@m.relationships "http://purl.org/dc/terms/replaces" "Replaces" />
    <@m.inverseRelationships "http://purl.org/dc/terms/replaces" "Replaced by"  />

    <@m.relationships "http://www.w3.org/2004/02/skos/core#narrower" "Child facilities" />
    <@m.inverseRelationships "http://www.w3.org/2004/02/skos/core#narrower" "Parent facilities"  />

    <@m.symmetricRelationships "http://purl.org/dc/terms/related" "Related"/>
  </#compress></div>

</@m.base>
