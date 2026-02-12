<div class="accessButtons"><#t/>
  <@exploreButton />
<#t/></div>

<#macro exploreButton>
  <#if apps?? && apps?size gte 1>
    <#list apps as app>
      <#assign containsLink = false>
      <#list collectionAccessButtonLink?split(',') as link>
        <#if app.url?contains(link?trim)>
          <#assign containsLink = true>
          <#break>
        </#if>
      </#list>
      <#if containsLink>
        <a class="btn btn-ukceh" href="${app.url}" alt="${app.name}" target="_blank" rel="noopener noreferrer">
          ${app.name}
          <span class="fa-solid fa-laptop"></span>
        </a>
      </#if>
    </#list>
  </#if>
</#macro>
