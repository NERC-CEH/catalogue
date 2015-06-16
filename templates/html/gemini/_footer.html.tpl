<div class="container">
<p>View as 
  <#if ['service', 'series', 'dataset']?seq_contains(resourceType) >
    <a href="${uri}.xml?format=gemini" class="label label-default">GEMINI 2.1 (XML)</a>
  <#else>
    <a href="${uri}.xml?format=gemini" class="label label-default">ISO 19115 (XML)</a>  
  </#if>
  <a href="${uri}.rdf?format=rdfxml" class="label label-default">RDF-XML</a>
  <a href="${uri}?format=json" class="label label-default">json</a>
</p>
</div>
