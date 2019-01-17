<#if supplemental?has_content>
  <#assign citationCount= citedBy?size + dataPapers?size>
  <#if citationCount gt 0 >
    <div class="panel panel-default hidden-print" id="document-metrics">
      <div class="panel-body">
        <div class="distribution-dataset">
         <h2><a href="#section-supplemental">
          <i class="fas fa-chart-line pull-right"></i>
          <span>${citationCount} <small>citation<#if  citationCount gt 1>s</#if></small></span>
          </a></h2>
        </div>
      </div>
    </div>
  </#if>
</#if>