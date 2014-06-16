<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title=title>
<!-- InstanceBeginEditable name="MAIN" -->
<div about="http://data.ceh.ac.uk/id/2a742df-3772-481a-97d6-0de5133f4812">

  <div class="page-header">
    <h2>${title}</h2>       
  </div>
</div>

<div>
  <#if topicCategories?has_content>
    <p>Topic category:</p>
    <#list topicCategories as topics>
      ${topics}
    </#list>
  </#if>
</div>

<div>
  <p>Keywords</p>
  <ul>

    <#list descriptiveKeywords as keywordsList>

      <#list keywordsList.keywords as keyword>
        <li>${keyword.value}</li>
      </#list>
    </#list>

  </ul>
</div>

<div>
  <p>Language:</p>
  <#if datasetLanguage?has_content>
    <p>${datasetLanguage.codeList}</p>
    <p>${datasetLanguage.value}</p>
  </#if>
</div>

<div>
  <#if alternateTitles?has_content>
    <p>Alternate title:</p>
    <#list alternateTitles as atitles>
      ${atitles}
    </#list>
  </#if>
</div>

<div>
  <#if metadata?has_content>
    <p>Metadata: ${metadata}</p>
  </#if>
</div>

<h2>${id}</h2>
</@skeleton.master>