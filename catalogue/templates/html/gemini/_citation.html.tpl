<#if citation?has_content>
  <div id="section-citation">
    <p><strong>If you reuse this data, you must cite</strong><br>
    <span id="citation-text">
      ${citation.authors?join('; ')?html} (${citation.year?string["0000"]?html}). ${citation.title?html}. ${citation.publisher?html}. <a href="${citation.url?html}">${citation.url?html}</a>
    </span></p>
    
    <div class="btn-group btn-group-xs hidden-xs" title="Import this citation into your reference management software">
      <a href="${citation.bibtex?html}" target="_blank" class="btn btn-default">BibTeX</a>
      <a href="${citation.ris?html}" target="_blank" class="btn btn-default">RIS</a>
    </div>
  </div>
  <p>&nbsp;</p>
</#if>
