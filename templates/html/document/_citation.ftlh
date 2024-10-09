<#if citation?has_content>
  <div>
    <p><strong>CITE AS: </strong>
    <span id="citation-text">
      ${citation.authors?join('; ')} (${citation.year?string["0000"]}). ${citation.title}. ${citation.publisher}. <a href="${citation.url}">${citation.url}</a>
    </span></p>
    <div class="btn-group btn-group-xs d-none d-sm-block" title="Import this citation into your reference management software">
      <a href="${citation.bibtex}" target="_blank" rel="noopener noreferrer" class="btn btn-light border">BibTeX</a>
      <a href="${citation.ris}" target="_blank" rel="noopener noreferrer" class="btn btn-light border">RIS</a>
    </div>
  </div>
</#if>
