<#if apps?? && apps?size gte 1>
  <#assign displayApp = apps?first>
  <#assign containsLink = false>
  <#list collectionAccessButtonLink?split(',') as link>
    <#if displayApp.url?contains(link?trim)>
      <#assign containsLink = true>
      <#break>
    </#if>
  </#list>
  <#if displayApp.url?has_content && !containsLink>
    <div class="dataExplorer d-none d-md-block">
      <div class="d-flex justify-content-end mt-2">
        <a href="${displayApp.url}" target="_blank" class="btn btn-sm btn-outline-secondary text-decoration-none">
          <span class="me-1">
            Open <#if displayApp.name?has_content>${displayApp.name} </#if>in new tab
          </span>
          <i class="fa-solid fa-up-right-from-square"></i>
        </a>
      </div>
      <iframe src="${displayApp.url}" title="Spatial Explorer" scrolling="no"></iframe>
    </div>
  </#if>
</#if>

