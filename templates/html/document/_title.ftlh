<#if citation?has_content>
  <div class="modal fade" tabindex="-1" role="dialog" id="citationModal">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-body">
          <button type="button" class="close" data-dismiss="modal" aria-label="CLose"><i aria-hidden="true" class="fa-solid fa-xmark"></i></button>
          <p><strong>Cite this dataset as</strong></p>
          <p id="citation-text">
            ${citation.authors?join('; ')} (${citation.year?string["0000"]}). <b>${citation.title}</b>. ${citation.publisher}.  (${codes.lookup('datacite.resourceTypeGeneral',citation.resourceTypeGeneral)}). ${citation.url}
          </p>
          <p><small>
            Import this citation into your reference management software:<br>
            &nbsp;&nbsp;&nbsp;&nbsp;<a href="${citation.bibtex}">BibTeX</a>&nbsp;&nbsp;|
            &nbsp;&nbsp;<a href="${citation.ris}">Reference Manager (RIS)</a>&nbsp;&nbsp;|
            &nbsp;&nbsp;<a href="${citation.ris}">Endnote</a>
          </small></p>
          <p><#include "_licence.ftlh"></p>
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
      <a class="pull-right" href="#" data-toggle="modal" data-target="#citationModal"><small><i class="fa-solid fa-quote-left fa-xs"></i> <i class="fa-solid fa-quote-right fa-xs"></i></small> Cite this ${recordType?lower_case!''}</a>
    </div>
  </#if>

  <#if resourceStatus?? && resourceStatus?has_content && resourceStatus != "Available" && resourceStatus != "Controlled"  && resourceStatus != "Unknown"  >
    <#assign notificationIcon = "fa-circle-info">
    <#if resourceStatus == "Restricted" >
      <#assign notificationIcon = "fa-triangle-exclamation ">
    </#if>
    <div class="notification notification--${resourceStatus}" role="alert">
      <div class="notification_icon"><i class="fas ${notificationIcon} fa-2x"></i></div>
      <div><#include "_status.ftlh"></div>
    </div>
  </#if>

  <#if resourceType?? && (resourceType.value == 'nercSignpost' || resourceType.value == 'signpost')>
    <div class="notice notice-signpost" role="alert">
      <div><i class="fa-solid fa-circle-info fa-2x"></i></div>
      <div>
        <#if resourceType.value == 'signpost' ><#include "_signpost.ftlh"></#if>
        <#if resourceType.value == 'nercSignpost'><#include "_nercSignpost.ftlh"></#if>
      </div>
    </div>
  </#if>

</div>
