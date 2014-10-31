<#if citation?has_content>
  <div id="section-authorsTop">
    <p><small>
      ${citation.authors?join(',')} (${citation.year?c}) <br>
      <a href="${citation.url}">${citation.doiDisplay}</a>
    </small></p>  
  </div>
</#if>