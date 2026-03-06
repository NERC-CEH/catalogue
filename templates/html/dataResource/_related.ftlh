
<#if (rel_partOf?has_content && rel_partOf?size gt 0) || (rel_isRequiredBy?has_content && rel_isRequiredBy?size gt 0) || (rel_requires?has_content && rel_requires?size gt 0) || (rel_relation?has_content && rel_relation?size gt 0) >

    <#macro displayRelatives id title array>
      <#if array?has_content && array?size gt 0>
        <div id="associations-${id}">
            <h4>${title}</h4>
            <#list array as item>
                <p class="${item.publicationStatus}"><a href="${item.href}">${item.title}</a></p>
            </#list>
        </div>
      </#if>
    </#macro>

    <div id="segment-associations">
      <h2>Related</h2>

      <#-- Related -->
      <@displayRelatives "related" "Related datasets" rel_relation />

      <#-- Services using this data resource -->
      <@displayRelatives "services" "Services associated with this dataset" rel_isRequiredBy />

      <#-- Data resources linked to this service -->
      <@displayRelatives "datasets" "Datasets associated with this service" rel_requires />

      <#-- PARENT record(s) -->
      <@displayRelatives "parents" "This ${recordType} is included in the following collections" rel_partOf />

    </div>
</#if>
