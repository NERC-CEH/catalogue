
<#if (rel_memberOf?has_content && rel_memberOf?size gt 0) || (rel_usedBy?has_content && rel_usedBy?size gt 0) || (rel_uses?has_content && rel_uses?size gt 0) || (rel_generatedBy?has_content && rel_generatedBy?size gt 0) || (rel_generates?has_content && rel_generates?size gt 0) || (rel_related?has_content && rel_related?size gt 0) >

    <#macro displayRelatives id title array>
      <#if array?has_content && array?size gt 0>
        <div id="associations-${id}">
            <h4>${title}</h4>
            <#list array as item>
                <p><a href="${item.href}">${item.title}</a></p>
            </#list>
        </div>
      </#if>
    </#macro>

    <div id="associations">
      <h2>Related</h2>
      <#-- Outputs of this model-->
      <#if resourceType.value=="application">
        <@displayRelatives "modelOutputs" "Model outputs" rel_generates />
      </#if>

      <#-- Related -->
      <@displayRelatives "related" "Related datasets" rel_related />

      <#-- Model(s) that generated this data -->
      <@displayRelatives "models" "Generated by" rel_generatedBy />

      <#-- Services using this data resource -->
      <@displayRelatives "services" "Services associated with this dataset" rel_usedBy />

      <#-- Data resources linked to this service -->
      <@displayRelatives "datasets" "Datasets associated with this service" rel_uses />

      <#-- PARENT record(s) -->
      <@displayRelatives "parents" "This ${recordType} is included in the following collections" rel_memberOf />

    </div>
</#if>
