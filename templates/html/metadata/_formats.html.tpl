<#if doc.distributionFormats?has_content>
<h4>Data are available in these formats:</h4>
<ul>
  <#list doc.distributionFormats as format>
    <li property="dc:format">${format.name?html}</li>
  </#list>
</ul>
</#if>