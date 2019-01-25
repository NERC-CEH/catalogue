<#if supplemental?has_content>
  <#assign citationCount= citedBy?size + dataPapers?size>
  <#if citationCount gt 0 >
    <div class="panel panel-default hidden-print" id="document-metrics">
      <div class="panel-body">
        <h2><a href="#section-supplemental">
        <i class="fas fa-chart-line pull-right"></i>
        This ${recordType} has <span>${citationCount}</span> citation<#if citationCount gt 1>s</#if>
        </a></h2>
        <#if citation?has_content>
          <p class="text-right"><small><a class="text-muted" href="#" data-toggle="modal" data-target="#citationModal"><i class="fas fa-quote-left fa-xs"></i> <i class="fas fa-quote-right fa-xs"></i> Cite this ${recordType?lower_case!''}</a></small></p>
        </#if>
      </div>
    </div>
  </#if>
</#if>