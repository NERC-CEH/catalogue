<#if citation?has_content>
  <div id="section-citation">
    <p>If you reuse this data, you must cite:</p>
    <p id="citation-text" property="dc:bibliographicCitation">
      ${citation.authors?join(',')?html} ${citation.year?html}. ${citation.title?html}. ${citation.publisher?html} ${citation.doiDisplay?html}
    </p>
    
    <div class="btn-group btn-group-xs hidden-xs" title="Import this citation into your reference management software">
      <a href="${citation.bibtex?html}" target="_blank" class="btn btn-default">BibTeX</a>
      <a href="${citation.ris?html}" target="_blank" class="btn btn-default">RIS</a>
    </div>
  </div>
</#if>
