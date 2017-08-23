<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.base>
  <#if format?? && format?has_content>
    <@b.key "Format" "Format of dataset">${format}</@b.key>
  </#if>
  <#if version?? && version?has_content>
    <@b.key "Version" "Version of dataset">${version}</@b.key>
  </#if>
  <#if parametersMeasured?? && parametersMeasured?has_content>
    <@b.key "Parameters Measured" "Parameters measured in dataset">
      <#list parametersMeasured as parameter>
        <@o.parametersMeasured parameter /> 
      </#list>
    </@b.key>
  </#if>
  <@o.relationships "Dependency" "Dependency between Datasets" "http://onto.nerc.ac.uk/CEHMD/rels/dependency" />
  <@o.relationships "Supercedes" "Datasets superceded by this Dataset" "http://onto.nerc.ac.uk/CEHMD/rels/supercedes" />
  <@o.relationships "Cites" "Citations for this Dataset" "http://onto.nerc.ac.uk/CEHMD/rels/cites" />
  <@o.relationships "Revises" "Dataset revisions" "http://onto.nerc.ac.uk/CEHMD/rels/revises" />
</@o.base>