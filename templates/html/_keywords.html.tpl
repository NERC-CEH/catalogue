<h3>Topic Categories</h3>
<#if topicCategories?has_content>
  <p>
    <#list topicCategories as topics>
      <span class="label label-primary">${topics}</span>
    </#list>
  </p>
</#if>
<h3>Keywords</h3>
<p>
  <#list descriptiveKeywords as keywordsList>
    <#list keywordsList.keywords as keyword>
      <span class="label label-primary">${keyword.value}</span>
    </#list>
  </#list>
</p>