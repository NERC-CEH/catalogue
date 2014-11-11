<#if citation?has_content>
  <div id="section-citation">
    <p>If you reuse this data, you must cite:</p>
    <p id="citation-text" property="dc:bibliographicCitation" about="_:0">
      ${citation.authors?join(',')} ${citation.year}. ${citation.title}. ${citation.publisher} ${citation.doiDisplay}
    </p>
    
    <div class="btn-group btn-group-xs hidden-xs" title="Import this citation into your reference management software">
      <a href="${citation.bibtex}" target="_blank" class="btn btn-default">BibTeX</a>
      <a href="${citation.ris}" target="_blank" class="btn btn-default">RIS</a>
    </div>
  </div>
</#if>
