<#--
  The following freemarker macro matches the javascript template
    /web/src/scripts/templates/facetResultsTemplate.js

  Where as the rest of the template matches up with
    /web/src/scripts/templates/facetsPanelTemplate.js
-->
<#macro facetResults facet>
  <select style="display: none;" class="form-select search-facet" multiple="multiple" data-placeholder="filter ${facet.displayName?lower_case}">
    <#list facet.results as facetItem>
      <#if facetItem.name?has_content && facetItem.name != 'Unknown' >
        <option <#if facetItem.active> selected="selected"</#if> value="${facetItem.url}">${facetItem.name} (${facetItem.count})</option>
      </#if>
    </#list>
  </select>
</#macro>

<#list facets as facet>
  <#if facet.results?size gt 0>
    <div class="facet">
      <div class="facet-header">${facet.displayName}</div>
      <@facetResults facet/>
    </div>
  </#if>
</#list>
