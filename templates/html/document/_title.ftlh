<#if citation?has_content>
  <div class="modal fade" tabindex="-1" role="dialog" id="citationModal">
    <div class="modal-dialog modal-fullscreen-sm-down modal-dialog-centered modal-citation" role="document">
      <div class="modal-content">
        <div class="modal-body">
          <button type="button" class="btn-close position-relative top-0 start-100 translate-middle" data-bs-dismiss="modal" aria-label="Close"></button>
          <#include "_citation.ftlh">
          <#include "_licence.ftlh">
        </div>
      </div>
    </div>
  </div>
</#if>

<div id="section-title" class="clearfix">
  <#if citation?has_content>
    <div class="authorList">
      <#if citation.authors?size gte 8>
        ${citation.authors?first} <em>et al</em>
      <#else>
        ${citation.authors?join('; ')}
      </#if>
    </div>
  </#if>

  <h1 id="document-title">
    <#if (metadata.state == 'draft' || metadata.state == 'pending') >
      <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
    </#if>
     <#if isLinkedDoc="true">
        <i class="fa-solid fa-link" title="linked document"></i>
      </#if>
    ${title}
    <#if alternateTitles?? && alternateTitles?has_content>
      <br><small class="alternateTitles">
        <#list alternateTitles as altTitle>
          (${altTitle})<#sep><br></#sep>
        </#list>
      </small>
    </#if>
  </h1>

  <#if citation?has_content>
    <div class="doi">
      <a href="${citation.url}">${citation.url}</a>
      <a class="float-end" href="#" data-bs-toggle="modal" data-bs-target="#citationModal"><small><i class="fa-solid fa-quote-left fa-xs"></i> <i class="fa-solid fa-quote-right fa-xs"></i></small> Cite this ${recordType?lower_case!''}</a>
    </div>
  </#if>

  <#if resourceType?? && (resourceType.value == 'nercSignpost' || resourceType.value == 'signpost')>
    <div class="notice notice-signpost" role="alert">
      <#if resourceType.value == 'signpost' ><#include "_signpost.ftlh"></#if>
      <#if resourceType.value == 'nercSignpost'><#include "_nercSignpost.ftlh"></#if>
    </div>
  </#if>

  <#if resourceStatus?? && resourceStatus?has_content && resourceStatus != "Available" && resourceStatus != "Controlled"  && resourceStatus != "Unknown"  >
    <div class="notice notice-${resourceStatus?lower_case}" role="alert">
      <#include "_status.ftlh">
    </div>
  </#if>
</div>
