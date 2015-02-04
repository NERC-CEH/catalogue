<#if citation?has_content>
  <div id="section-authorsTop">
    <p><small>
      ${citation.authors?join(',')?html} (${citation.year?c}) <br>
      <a href="${citation.url?html}">${citation.doiDisplay?html}</a>
    </small></p>  
  </div>
</#if>