<#import "_base.ftlh" as base>
<#import "_macros.ftlh" as m>

<@base.base>
  <@m.relationships "Part Of" "Part of another Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
  <@m.inverseRelationships "Used By" "Used by this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.inverseRelationships "Responsible for" "Agents responsible for this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/responsibleFor" />
  <@m.inverseRelationships "Has Part" "Monitoring Facility has parts" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
</@base.base>
