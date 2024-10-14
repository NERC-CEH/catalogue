<#import "monitoringBase.ftlh" as m>
<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <@m.map boundingBox.bounds />
    </@m.keyValue>
  </#if>


  <div class="relationships">
    <@m.relationships "http://onto.ceh.ac.uk/EF#utilises" "Utilises"/>

    <@m.symmetricRelationships "http://onto.ceh.ac.uk/EF#associatedWith" "Related"/>

    <@m.relationships  "http://onto.ceh.ac.uk/EF#supersedes" "Supersedes" />
    <@m.inverseRelationships  "http://onto.ceh.ac.uk/EF#supersedes" "Superseded by"/>

    <@m.relationships "http://onto.nerc.ac.uk/CEHMD/rels/triggers" "Activities set up for this programme"  />
  </div>
</@m.base>
