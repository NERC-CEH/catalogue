<#if supplemental?has_content>
  <#assign referenceCount= referencedBy?size + dataPapers?size>
  <#if referenceCount gt 0 >
    <div class="panel panel-default hidden-print" id="document-metrics">
      <div class="panel-body">
        <h2><a href="#section-supplemental">
        <i class="fas fa-book-reader pull-right"></i>
        <span>${referenceCount}</span> citation<#if referenceCount gt 1>s</#if>
        </a></h2>
      </div>
    </div>
  </#if>
</#if>