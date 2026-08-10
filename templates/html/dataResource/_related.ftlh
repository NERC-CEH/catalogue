
<#if (relPartOf?has_content && relPartOf?size gt 0) || (relIsRequiredBy?has_content && relIsRequiredBy?size gt 0) || (relRequires?has_content && relRequires?size gt 0) || (relRelation?has_content && relRelation?size gt 0) >

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
      <@displayRelatives "related" "Related datasets" relRelation />

      <#-- Services using this data resource -->
      <@displayRelatives "services" "Services associated with this dataset" relIsRequiredBy />

      <#-- Data resources linked to this service -->
      <@displayRelatives "datasets" "Datasets associated with this service" relRequires />

      <#-- PARENT record(s) -->
      <@displayRelatives "parents" "This ${recordType} is included in the following collections" relPartOf />

    </div>
</#if>
