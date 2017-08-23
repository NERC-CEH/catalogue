<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.base>
  <#if facilityType?? && facilityType?has_content>
    <@b.key "Type" "Type of Monitoring Facility">${facilityType}</@b.key>
  </#if>
  <@o.relationships "Location" "Location of Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/located" />
  <@o.relationships "Part Of" "Part of another Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
  <@o.inverseRelationships "Used" "Monitoring Activities using this Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
</@o.base>