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
        <a href="${facet.url}">${facet.name}</a> <small class="text-muted">(${facet.count})</small>
        <#if facet.active >
          <a href="${facet.url}">
            <span class="fas fa-times"></span>
          </a>
        </#if>
        <#if facet.subFacetResults?has_content>
          <@facetResults facet.subFacetResults/>
        </#if>
      </li>
    </#list>
  </ul>
</#macro> 


<#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
<div class="adminFacets">
<h3><a data-toggle="collapse" href="#facets" aria-expanded="false" aria-controls="facets" title="Show/hide admin filters">Admin</a></h3>
  <div id="facets" class="collapse">
  <#list facets as facet>
    <#if facet.admin>
      <div class="facet">
        <h3>${facet.displayName}</h3>
        <@facetResults facet.results/>
      </div>
    </#if>
  </#list>
</div></div>
</#if>

<#list facets as facet>
  <#if !facet.admin>
    <div class="facet">
      <h3>${facet.displayName}</h3>
      <@facetResults facet.results/>
    </div>
  </#if>
</#list>