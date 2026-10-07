<#-- The relationships section of network.ftlh. An include so a test can render it without the
     base macro (MonitoringRelationshipsTemplateTest). Expects monitoringBase.ftlh imported as "m". -->
<div class="relationships">

  <@m.inverseRelationships relUsedBy "Used by"  />
  <@m.inverseRelationships utilisingProgrammes "Used by"  />
  <@m.inverseRelationships producedDataResources "Data produced by this network"  />

  <@m.relationships relSupersedes "Supersedes" />
  <@m.inverseRelationships relSupersededBy "Superseded by"  />

  <@m.relationships relChildNetwork "Child networks" />
  <@m.inverseRelationships relParentNetwork "Parent networks"  />

  <@m.symmetricRelationships relRelated "Related"/>
</div>
