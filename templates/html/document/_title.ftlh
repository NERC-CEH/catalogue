<#if citation?has_content>
  <div class="modal fade" tabindex="-1" role="dialog" id="citationModal">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-body">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close"><i aria-hidden="true" class="fas fa-times"></i></button>
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

  <#if resourceType?? && resourceType.value == 'signpost' >
    <#include "_signpost_message.ftlh">
  <#elseif resourceType?? && resourceType.value == 'signpostNerc' >
    <span class="recordType recordType--signpostNerc">NERC signpost record</span>
  </#if>

  <h1 id="document-title">
    <#if (metadata.state == 'draft' || metadata.state == 'pending') >
      <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
    </#if>
     <#if isLinkedDoc="true"> 
        <i class="fas fa-link" title="linked document"></i>
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
      <a class="pull-right" href="#" data-toggle="modal" data-target="#citationModal"><small><i class="fas fa-quote-left fa-xs"></i> <i class="fas fa-quote-right fa-xs"></i></small> Cite this ${recordType?lower_case!''}</a>
    </div>
  </#if>

  <#if resourceStatus??>
    <#include "_status.ftlh">
  </#if>
  
</div>
