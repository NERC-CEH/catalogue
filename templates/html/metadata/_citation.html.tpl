<#if doc.citation?has_content>
  <div id="section-citation">
    <h4>If you reuse this data, you must cite:</h4>
    <p id="citation-text" property="dc:bibliographicCitation">
      ${doc.citation.authors?join(',')?html} ${doc.citation.year?html}. ${doc.citation.title?html}. ${doc.citation.publisher?html} ${doc.citation.doiDisplay?html}
    </p>
    
    <div class="btn-group btn-group-xs hidden-xs" title="Import this citation into your reference management software">
      <a href="${doc.citation.bibtex?html}" target="_blank" class="btn btn-default">BibTeX</a>
      <a href="${doc.citation.ris?html}" target="_blank" class="btn btn-default">RIS</a>
    </div>
  </div>
</#if>
