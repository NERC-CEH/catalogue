<#assign authorlist = "">
<#if citation?has_content>
  <#if citation.authors?size gte 8>
    <#assign authorlist = citation.authors?first + "<i>et al</i>">
  <#else>
    <#assign authorlist =  citation.authors?join('; ') > 
  </#if>

  <div class="modal fade" tabindex="-1" role="dialog" id="citationModal">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-body">
          <button type="button" class="close" data-dismiss="modal" aria-label="CLose"><i aria-hidden="true" class="fa fa-times"></i></button>
          <p><strong>Cite this dataset as</strong></p>
          <p id="citation-text">
            ${citation.authors?join('; ')?html} (${citation.year?string["0000"]?html}). <b>${citation.title?html}</b>. ${citation.publisher?html}.  ${citation.url?html}
          </p>
          <p><small>
            Import this citation into your reference management software:<br>
            &nbsp;&nbsp;&nbsp;&nbsp;<a href="${citation.bibtex?html}">BibTeX</a>&nbsp;&nbsp;|
            &nbsp;&nbsp;<a href="${citation.ris?html}">Reference Manager (RIS)</a>&nbsp;&nbsp;|
            &nbsp;&nbsp;<a href="${citation.ris?html}">Endnote</a>
          </small></p>
          <p><#include "_licence.html.tpl"></p>
        </div>
      </div>
    </div>
  </div>

</#if>

<div id="section-title" class="clearfix">
  <div class="authorList">${authorlist}</div>
  <h1>
    <#if (metadata.state == 'draft' || metadata.state == 'pending') >
      <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
    </#if>
    ${title}
  </h1>
  <#if citation?has_content>
    <div class="doi">
      <a href="${citation.url?html}">${citation.url?html}</a>
      <a class="pull-right" href="#" data-toggle="modal" data-target="#citationModal"><i class="fa fa-bookmark"></i> Cite this ${resourceType.value}</a>
    </div>
  </#if>
  <#include "_notCurrent.html.tpl">
</div>