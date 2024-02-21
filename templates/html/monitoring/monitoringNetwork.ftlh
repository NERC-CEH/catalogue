<#import "base.ftlh" as o>
<#import "../blocks.ftlh" as b>

<@o.base>
  <#assign inverseRelationshipCombinedGeometriesOutput>
    <@o.inverseRelationshipCombinedGeometries "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
  </#assign>
  <#if inverseRelationshipCombinedGeometriesOutput?has_content>
    <@b.key "Location" "Monitoring Network">
      ${inverseRelationshipCombinedGeometriesOutput}
    </@b.key>
  </#if>

  <@o.relationships "Part Of" "Part of another Monitoring Facility" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
  <@o.inverseRelationships "Used By" "Used by this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
  <@o.inverseRelationships "Responsible for" "Agents responsible for this Monitoring Activity" "http://onto.nerc.ac.uk/CEHMD/rels/responsibleFor" />
  <@o.inverseRelationships "Contains" "Monitoring Network contains" "http://onto.nerc.ac.uk/CEHMD/rels/partOf" />
</@o.base>
