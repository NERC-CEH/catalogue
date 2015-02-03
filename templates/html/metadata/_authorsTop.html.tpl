<#if doc.citation?has_content>
  <div id="section-authorsTop">
    <p><small>
      ${doc.citation.authors?join(',')?html} (${doc.citation.year?c}) <br>
      <a href="${doc.citation.url?html}">${doc.citation.doiDisplay?html}</a>
    </small></p>  
  </div>
</#if>