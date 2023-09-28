<#--
  The following freemarker macro matches the javascript template
    /web/src/scripts/templates/facetResultsTemplate.js

  Where as the rest of the template matches up with
    /web/src/scripts/templates/facetsPanelTemplate.js
-->
<#macro facetResults results>
  <ul>
    <#list results as facet>
      <#if facet.name?has_content && facet.name != 'Unknown' >
        <li>
          <a href="${facet.url}" title="${facet.name} (${facet.count})">${facet.name} <small class="facet-count">(${facet.count})</small></a>
          <#if facet.active >
            <a href="${facet.url}" title="${facet.name} (${facet.count})">
              <span class="fa-solid fa-xmark"></span>
            </a>
          </#if>
          <#if facet.subFacetResults?has_content>
            <@facetResults facet.subFacetResults/>
          </#if>
        </li>
      </#if>
    </#list>
  </ul>
</#macro>

<#list facets as facet>
  <#if facet.results?size gt 0>
  <div class="facet">
    <h3>${facet.displayName}</h3>
    <@facetResults facet.results/>
  </div>
  </#if>
</#list>
