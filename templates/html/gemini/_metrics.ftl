<#if supplemental?has_content && catalogue.id=="eidc">
  <#assign referenceCount= referencedBy?size + supplementTo?size>
  <#if referenceCount gt 0 >
    <div id="document-metrics">
      <h2><a href="#section-supplemental">
      <i class="fas fa-book-reader pull-right"></i>
      <span>${referenceCount}</span> citation<#if referenceCount gt 1>s</#if>
      </a></h2>
    </div>
  </#if>
</#if>