<#import "monitoringBase.ftlh" as m>

<@m.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@m.inverseRelationshipCombinedGeometries "http://onto.ceh.ac.uk/EF#belongsTo" />
  </#assign>

  <div class="networkMap">
    <#if inverseRelationshipCombinedGeometriesOutput?has_content>
     <div>${inverseRelationshipCombinedGeometriesOutput}</div>
    </#if>
    <div class="facilitiesList">
      <@m.inverseRelationships "Contains" "http://onto.ceh.ac.uk/EF#belongsTo"/>
    </div>
  </div>

  <div class="relationships">

    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#uses" "Used by"  />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#utilises" "Used by"  />

    <@m.relationships "http://onto.ceh.ac.uk/EF#supersedes" "Supersedes" />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#supersedes" "Superseded by"  />

    <@m.relationships "http://onto.ceh.ac.uk/EF#narrower" "Child networks" />
    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#narrower" "Parent networks"  />

    <@m.symmetricRelationships "http://onto.ceh.ac.uk/EF#associatedWith" "Related"/>
  </div>

</@m.base>
