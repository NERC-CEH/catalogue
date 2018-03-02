<#import "../blocks.html.tpl" as b>
<#import "osdp.html.tpl" as o>

<@o.base>
  <#if address?? && address?has_content>
    <@b.key "Address" "">
      <@b.linebreaks address />
    </@b.key>
  </#if>
  <@o.relationships "Creates" "Research Artifacts created by this Agent" "http://onto.nerc.ac.uk/CEHMD/rels/creates" />
  <@o.relationships "Knows" "Agents known by this Agent" "http://onto.nerc.ac.uk/CEHMD/rels/knows" />
  <@o.relationships "Associated With" "Monitoring Activities & Programmes associated with this Agent" "http://onto.nerc.ac.uk/CEHMD/rels/associatedWith" />
  <@o.relationships "Responsible For" "Monitoring Facilities the repsonsiblity of this Agent" "http://onto.nerc.ac.uk/CEHMD/rels/responsibleFor" />
</@o.base>