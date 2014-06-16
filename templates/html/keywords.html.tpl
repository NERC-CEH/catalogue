<#macro keywords>
<!-- InstanceBeginEditable name="MAIN" -->
<div>
  <p><b>Keywords</b></p>
  <ul>

    <#list descriptiveKeywords as keywordsList>

      <#list keywordsList.keywords as keyword>
        <li>${keyword.value}</li>
      </#list>
    </#list>

  </ul>
</div>
</#macro>