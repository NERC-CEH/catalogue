<#if citation?has_content>
  <div class="cite">
    <p>
    Cite this ${recordType?lower_case!''} as:
    <span>${citation.authors?join('; ')} (${citation.year?string["0000"]}). <b>${citation.title}</b>. ${citation.publisher}. <a href="${citation.url}">${citation.url}</a></span>
    </p>
    <div class="btn-group btn-group-sm d-print-none" role="group" aria-label="Import into reference management software" title="Import this citation into your reference management software">
      <a href="${citation.bibtex}" target="_blank" rel="noopener noreferrer" class="btn btn-light border">BibTeX</a>
      <a href="${citation.ris}" target="_blank" rel="noopener noreferrer" class="btn btn-light border">RIS</a>
    </div>
  </div>
</#if>
