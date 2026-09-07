<#import "monitoringBase.ftlh" as m>

<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <div class="mapper">
        <@m.map boundingBox.bounds />
      </div>
    </@m.keyValue>

  </#if>

  <@m.relationships relUseNetworkOrFacility "Uses these facilities/networks"/>
  <@m.inverseRelationships relSetupForProgramme "Set up for these programmes"/>

</@m.base>



