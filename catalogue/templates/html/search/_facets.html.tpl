<#--
  The following freemarker macro matches the javascript template 
    /web/src/scripts/templates/FacetResults.tpl

  Where as the rest of the template matches up with
    /web/src/scripts/templates/FacetsPanel.tpl
-->
<#macro facetResults results>
  <ul>
    <#list results as facet>
      <li>
        <a href="${facet.url}">${facet.name}</a> <small class="text-muted">(${facet.count})</small></span>
        <#if facet.active >
          <a href="${facet.url}">
            <span class="glyphicon glyphicon-remove"></span>
          </a>
        </#if>
        <#if facet.subFacetResults?has_content>
          <@facetResults facet.subFacetResults/>
        </#if>
      </li>
    </#list>
  </ul>
</#macro> 

<#list facets as facet>
  <h3>${facet.displayName}</h3>
  <@facetResults facet.results/>
</#list>