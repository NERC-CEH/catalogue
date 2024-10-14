<#import "monitoringBase.ftlh" as m>

<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <div class="mapper">
        <@m.map boundingBox.bounds />
      </div>
    </@m.keyValue>

  </#if>

  <@m.relationships "http://onto.nerc.ac.uk/CEHMD/rels/uses" "Uses these facilities/networks"/>
  <@m.relationships "http://ontm.nerc.ac.uk/CEHMD/rels/setupFor" "Set up for these programmes"/>

  <@m.inverseRelationships  "http://onto.nerc.ac.uk/CEHMD/rels/triggers"  "Set up for these programmes"/>

</@m.base>



