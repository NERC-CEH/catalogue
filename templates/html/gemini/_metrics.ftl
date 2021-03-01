<#if supplemental?has_content && catalogue.id=="eidc">
  <#if incomingReferenceCount gt 0 >
    <div id="document-metrics">
      <h2><a href="#section-supplemental">
      <i class="fas fa-book-reader pull-right"></i>
      <span>${incomingReferenceCount}</span> citation<#if incomingReferenceCount gt 1>s</#if>
      </a></h2>
    </div>
  </#if>
</#if>