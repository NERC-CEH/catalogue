<#if topicCategories?has_content>
<h3>Topic Categories</h3>
<div class="document-categories">
  <#list topicCategories as topics>
    <span class="label label-default">${topics}</span>
  </#list>
</div>
</#if>
<#if descriptiveKeywords?has_content>
<h3>Keywords</h3>
<div id="document-keywords" property="dc:subject" class="document-keywords">
  <#list descriptiveKeywords as keywordsList>
    <#list keywordsList.keywords as keyword>
      <#if keyword.URI?has_content>
        <a class="btn btn-default btn-xs" href="${keyword.URI}" target="_blank">${keyword.value} <span  class="glyphicon glyphicon-link"></span></a>
      <#else>
        <span class="label label-default">${keyword.value}</span>
      </#if>
    </#list>
  </#list>
</div>
</#if>
