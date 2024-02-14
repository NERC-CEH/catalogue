<#import "_base.ftlh" as base>
<#import "_macros.ftlh" as m>

<@base.base>

   <#if temporalExtent?? && temporalExtent?has_content>
     <@m.dataRow "Temporal Extent"><@m.temporalExt temporalExtent /></@m.dataRow>
  </#if>

  <#if geometry?? && geometry?has_content>
    <@m.dataRow "Location">
      <@m.geometry geometry.geometryString />
    </@m.dataRow>
  </#if>

  <@m.relationships "Part Of" "Part of another Monitoring Facility" "http://ontm.nerc.ac.uk/CEHMD/rels/partOf" />
  <@m.inverseRelationships "Used By" "Used by this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.inverseRelationships "Responsible for" "Agents responsible for this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/responsibleFor" />
  <@m.inverseRelationships "Has Part" "Monitoring Facility has parts" "http://ontm.nerc.ac.uk/CEHMD/rels/partOf" />
</@base.base>
