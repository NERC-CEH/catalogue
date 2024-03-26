<#import "monitoringBase.ftlh" as m>

<@m.base>

  <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <div class="mapper">
        <@m.map boundingBox.bounds />
      </div>
    </@m.keyValue>

  </#if>

  <@m.inverseRelationships "Associated With" "Agents associated with this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/associatedWith" />
  <@m.relationships "Produces" "Research Artifacts produced by this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/produces" />
  <@m.relationships "Uses" "Monitoring Facilities used by this Monitoring Activity" "http://ontm.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.inverseRelationships "Used By" "Used by this artifact" "http://ontm.nerc.ac.uk/CEHMD/rels/uses" />
  <@m.relationships "Setup for" "This activity was set up for these Monitoring Programmes" "http://ontm.nerc.ac.uk/CEHMD/rels/setupFor" />
</@m.base>



