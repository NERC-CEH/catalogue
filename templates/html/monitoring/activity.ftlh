<#import "monitoringBase.ftlh" as m>

<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <div class="mapper">
        <@m.map boundingBox.bounds />
      </div>
    </@m.keyValue>

  </#if>

  <@m.relationships useNetworkOrFacility "Uses these facilities/networks"/>
  <@m.inverseRelationships setupForProgramme "Set up for these programmes"/>

</@m.base>



