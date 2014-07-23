<#if topicCategories?has_content>
<h3>Topic Categories</h3>
<div class="document-categories">
  <#list topicCategories as topics>
    <span class="label label-primary">${topics}</span>
  </#list>
</div>
</#if>
<#if descriptiveKeywords?has_content>
<h3>Keywords</h3>
<div id="document-keywords" property="dc:subject" class="document-keywords">
  <#list descriptiveKeywords as keywordsList>
    <#list keywordsList.keywords as keyword>
      <span class="label label-primary">${keyword.value}</span>
    </#list>
  </div>
</#if>
