<#import "_base.ftlh" as base>
<#import "_macros.ftlh" as m>

<@base.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.geometry boundingBox.bounds/>
  </#if>

  <@m.inverseRelationships "Associated With" "Agents associated with this Monitoring Activity" "http://ontbase.nerc.ac.uk/CEHMD/rels/associatedWith" />
  <@m.relationships "Produces" "Research Artifacts produced by this Monitoring Activity" "http://ontbase.nerc.ac.uk/CEHMD/rels/produces" />
  <@m.relationships "Uses" "Monitoring Facilities used by this Monitoring Activity" "http://ontbase.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.inverseRelationships "Used By" "Used by this artifact" "http://ontbase.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.relationships "Setup for" "This activity was set up for these Monitoring Programmes" "http://ontbase.nerc.ac.uk/CEHMD/rels/setupFor" />
</@base.base>



