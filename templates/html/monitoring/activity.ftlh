<#import "monitoringBase.ftlh" as m>

<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <div class="mapper">
        <@m.map boundingBox.bounds />
      </div>
    </@m.keyValue>

  </#if>

  <@m.relationships "Uses" "Uses these facilities/networks" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.relationships "Setup for" "Set up for these programmes" "http://ontm.nerc.ac.uk/CEHMD/rels/setupFor" />
  <@m.inverseRelationships "Setup for" "Set up for these programmes" "http://onto.nerc.ac.uk/CEHMD/rels/triggers" />

</@m.base>



