<div class="accessButtons"><#t/>
  <@exploreButton />
<#t/></div>

<#macro exploreButton>
  <#if apps?? && apps?size gte 1>
    <#list apps as app>
      <#if app.url?contains('/explore/data')>
        <a class="btn btn-ukceh" href="${app.url}" alt="${app.name}" target="_blank" rel="noopener noreferrer">
          ${app.name}
          <span class="fa-solid fa-laptop"></span>
        </a>
      </#if>
    </#list>
  </#if>
</#macro>
