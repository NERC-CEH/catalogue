<#import "monitoringBase.ftlh" as m>
<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <@m.map boundingBox.bounds />
    </@m.keyValue>
  </#if>

  <@m.relationships "Triggers" "Activities set up for this programme" "http://onto.nerc.ac.uk/CEHMD/rels/triggers" />


</@m.base>
