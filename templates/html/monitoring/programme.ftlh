<#import "monitoringBase.ftlh" as m>
<@m.base>

  <#assign CombinedGeometriesOutput>
    <@m.programmeCombinedGeometries "http://onto.ceh.ac.uk/EF#utilises" />
  </#assign>


  <#if CombinedGeometriesOutput?has_content>
    <div class="childFeatures">
      <div class="featureMap">${CombinedGeometriesOutput}</div>
      <div class="featureList">
        <@m.relationships "http://onto.ceh.ac.uk/EF#utilises" "Uses"  />
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
    <#if !CombinedGeometriesOutput?has_content><@m.relationships "http://onto.ceh.ac.uk/EF#utilises" "Uses"  /></#if>

    <@m.relationships  "http://onto.ceh.ac.uk/EF#supersedes" "Supersedes" />
    <@m.inverseRelationships  "http://onto.ceh.ac.uk/EF#supersedes" "Superseded by"/>

    <@m.relationships "http://onto.ceh.ac.uk/EF#triggers" "Activities set up for this programme"  />

    <@m.inverseRelationships "http://onto.ceh.ac.uk/EF#hasChild" "Parent programmes"  />
    <@m.relationships "http://onto.ceh.ac.uk/EF#hasChild" "Child programmes"  />

    <@m.symmetricRelationships "http://onto.ceh.ac.uk/EF#associatedWith" "Related"/>
  </div>
</@m.base>
