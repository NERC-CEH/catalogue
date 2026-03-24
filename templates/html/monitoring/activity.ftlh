<#import "monitoringBase.ftlh" as m>

<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <div class="mapper">
        <@m.map boundingBox.bounds />
      </div>
    </@m.keyValue>

  </#if>

  <@m.relationships "https://digital.ceh.ac.uk/ontology/doo/uses" "Uses these facilities/networks"/>
  <@m.inverseRelationships  "https://digital.ceh.ac.uk/ontology/doo/triggers"  "Set up for these programmes"/>

</@m.base>



