<#import "base.ftlh" as o>
<#import "../blocks.ftlh" as b>

<@o.base>
  <@o.relationships "Part Of" "Part of another Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
  <@o.inverseRelationships "Used By" "Used by this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@o.inverseRelationships "Responsible for" "Agents responsible for this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/responsibleFor" />
  <@o.inverseRelationships "Has Part" "Monitoring Facility has parts" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
</@o.base>
