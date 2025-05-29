<#if resourceStatus?? && resourceStatus?has_content && resourceStatus != "Available" && resourceStatus != "Controlled"  && resourceStatus != "Unknown"  >
  <div class="alert alert-primary" role="alert">
    <#include "_status.ftlh">
  </div>
</#if>

<div id="segment-title" class="mt-2 border-bottom border-1">
  <#if citation?has_content>
    <div class="authorList text-body-secondary">
      <#if citation.authors?size gte 8>
        ${citation.authors?first} <em>et al</em>
      <#else>
        ${citation.authors?join('; ')}
      </#if>
    </div>
  </#if>

  <h1>
    <#if (metadata.state == 'draft' || metadata.state == 'pending') >
      <span class="fs-6 badge text-bg-danger">${codes.lookup('publication.state', metadata.state)?upper_case!''}</span>
    </#if>
     <#if isLinkedDoc>
        <i class="fa-solid fa-2xs fa-link text-body-tertiary" title="linked document"></i>
      </#if>
    ${title}
    <#if alternateTitles?? && alternateTitles?has_content>
      <br><small class="text-secondary fs-5 ">
        <#list alternateTitles as altTitle>
          (${altTitle})<#sep><br></#sep>
        </#list>
      </small>
    </#if>
  </h1>

  <#if citation?has_content>
    <div class="topDOI">${citation.url}</div>
  </#if>

</div>

<#if resourceType?? && (resourceType.value == 'nercSignpost' || resourceType.value == 'signpost')>
  <div class="alert alert-secondary" role="alert">
    <#if resourceType.value == 'signpost' ><#include "_signpost.ftlh"></#if>
    <#if resourceType.value == 'nercSignpost'><#include "_nercSignpost.ftlh"></#if>
  </div>
</#if>
