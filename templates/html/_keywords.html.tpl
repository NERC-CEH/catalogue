<div>
  <h3>Keywords</h3>
  <p>
    <#list descriptiveKeywords as keywordsList>
      <#list keywordsList.keywords as keyword>
        ${keyword.value}<br>
      </#list>
    </#list>
  </p>
</div>