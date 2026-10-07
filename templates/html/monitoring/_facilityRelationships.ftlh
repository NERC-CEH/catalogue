<#-- The relationships section of facility.ftlh. An include so a test can render it without the
     base macro (MonitoringRelationshipsTemplateTest). Expects monitoringBase.ftlh imported as "m". -->
<div class="relationships"><#compress>
  <@m.relationships relBelongsToNetwork "Belongs to these networks"/>

  <@m.inverseRelationships relUsedBy "Used by"  />
  <@m.inverseRelationships utilisingProgrammes "Used by"  />
  <@m.inverseRelationships producedDataResources "Data produced here"  />

  <@m.relationships relSupersedes "Supersedes" />
  <@m.inverseRelationships relSupersededBy "Superseded by"  />

  <@m.relationships relChildFacility "Child facilities" />
  <@m.inverseRelationships relParentFacility "Parent facilities"  />

  <@m.symmetricRelationships relRelated "Related"/>
</#compress></div>
