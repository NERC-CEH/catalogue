<#if citation?has_content>
  <div id="section-authorsTop"><p><small>

  <#if citation.authors?size gte 10>
    ${citation.authors?first?html} 
	<i>et al</i>
   <#else>
   ${citation.authors?join('; ')?html} 
   </#if>

   (${citation.year?c})<br>
	  <a href="${citation.url?html}">${citation.doiDisplay?html}</a>
    </small></p></div>
</#if>